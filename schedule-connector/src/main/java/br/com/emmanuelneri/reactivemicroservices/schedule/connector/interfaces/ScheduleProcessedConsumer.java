package br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaConsumerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.ScheduleConnectorEvents;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import java.util.Map;

public class ScheduleProcessedConsumer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleProcessedConsumer.class);
    private static final String CONSUMER_GROUP_ID = "SCHEDULE_CONNECTOR_GROUP";
    private static final String PROCESSED_RECEIVED_EVENT = ScheduleConnectorEvents.SCHEDULE_PROCESSED_RECEIVED.getName();
    public static final String SCHEDULE_PROCESSED_TOPIC = "ScheduleProcessed";

    private final KafkaConsumerConfiguration configuration;

    public ScheduleProcessedConsumer(final KafkaConsumerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start(final Future<Void> startFuture) {
        final Map<String, String> config = configuration.createConfig(CONSUMER_GROUP_ID);
        final KafkaConsumer<String, String> kafkaConsumer = KafkaConsumer.create(vertx, config);

        kafkaConsumer.subscribe(SCHEDULE_PROCESSED_TOPIC, subscribeHandler());
        kafkaConsumer.handler(this::send);
    }

    private void send(final KafkaConsumerRecord<String, String> message) {
        LOGGER.info("message received {0}", message.value());
        vertx.eventBus().send(PROCESSED_RECEIVED_EVENT, message.value());
    }

    private Handler<AsyncResult<Void>> subscribeHandler() {
        return result -> {
            if (result.failed()) {
                LOGGER.error("failed to subscribe {0} topic", SCHEDULE_PROCESSED_TOPIC, result.cause());
                return;
            }

            LOGGER.info("{0} topic subscribed", SCHEDULE_PROCESSED_TOPIC);
        };
    }
}