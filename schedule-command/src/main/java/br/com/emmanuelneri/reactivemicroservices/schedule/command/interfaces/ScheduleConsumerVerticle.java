package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaConsumerConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecords;

import java.util.Map;

public class ScheduleConsumerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleConsumerVerticle.class);
    private static final String CONSUMER_GROUP_ID = "SCHEDULE_GROUP";
    static final String SCHEDULE_REQUEST_TOPIC = "ScheduleRequested";

    private final KafkaConsumerConfiguration configuration;

    public ScheduleConsumerVerticle(final KafkaConsumerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start(final Future<Void> startFuture) {
        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
        final Map<String, String> config = configuration.createConfig(CONSUMER_GROUP_ID);
        final KafkaConsumer<String, String> kafkaConsumer = KafkaConsumer.create(vertx, config);

        kafkaConsumer.subscribe(SCHEDULE_REQUEST_TOPIC, result -> {
            if (result.failed()) {
                LOGGER.error("failed to subscribe {0} topic", SCHEDULE_REQUEST_TOPIC, result.cause());
                return;
            }

            LOGGER.info("{0} topic subscribed", SCHEDULE_REQUEST_TOPIC);
        });

        kafkaConsumer.handler(consumerRecord ->
                LOGGER.info("message received. offset: {0} - key: {1}", consumerRecord.offset(), consumerRecord.key()));

        kafkaConsumer.batchHandler(batch -> {
            try {
                batch.records().forEach(scheduleMessageProcessor::process);
                kafkaConsumer.commit();
                LOGGER.info("batch committed. size: {0}", batch.size());
            } catch (final Exception ex) {
                LOGGER.error("batch error. size: {0} - records: {1}", batch.size(), log(batch), ex);
            }
        });
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
