package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaConsumerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.usecases.ScheduleProcessor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecords;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScheduleConsumerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleConsumerVerticle.class);
    private static final String CONSUMER_GROUP_ID = "SCHEDULE_COMMAND_GROUP";
    public static final String SCHEDULE_REQUEST_TOPIC = "ScheduleRequested";

    private final KafkaConsumerConfiguration configuration;

    public ScheduleConsumerVerticle(final KafkaConsumerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start(final Future<Void> startFuture) {
        final ScheduleProcessor scheduleProcessor = ScheduleProcessor.create(this.vertx);
        final Map<String, String> config = configuration.createConfig(CONSUMER_GROUP_ID);
        final KafkaConsumer<String, String> kafkaConsumer = KafkaConsumer.create(vertx, config);

        kafkaConsumer.subscribe(SCHEDULE_REQUEST_TOPIC, subscribeHandler());

        kafkaConsumer.handler(consumerRecord ->
                LOGGER.info("message received. offset: {0} - key: {1}", consumerRecord.offset(), consumerRecord.key()));

        kafkaConsumer.batchHandler(batch -> {
            final List<Promise<Void>> promises = new LinkedList<>();
            batch.records().forEach(record -> {
                final Promise<Void> promise = Promise.promise();
                scheduleProcessor.process(record, promise);
                promises.add(promise);
            });

            CompositeFuture.all(promises.stream().map(Promise::future).collect(Collectors.toList()))
                    .setHandler(compositeResultHandler -> {
                        if (compositeResultHandler.failed()) {
                            LOGGER.error("batch error. size: {0} - records: {1}",
                                    batch.size(), log(batch), compositeResultHandler.cause());
                            return;
                        }

                        kafkaConsumer.commit();
                        LOGGER.info("batch committed. size: {0}", batch.size());
                    });
        });
    }

    private Handler<AsyncResult<Void>> subscribeHandler() {
        return result -> {
            if (result.failed()) {
                LOGGER.error("failed to subscribe {0} topic", SCHEDULE_REQUEST_TOPIC, result.cause());
                return;
            }

            LOGGER.info("{0} topic subscribed", SCHEDULE_REQUEST_TOPIC);
        };
    }

    private String log(final KafkaConsumerRecords<String, String> batch) {
        final StringBuilder recordValues = new StringBuilder();
        batch.records().forEach(record -> {
            recordValues.append("offset: ");
            recordValues.append(record.offset());
            recordValues.append(",");
            recordValues.append("body: ");
            recordValues.append(record.value());
            recordValues.append(" | ");
        });
        return recordValues.toString();
    }
}
