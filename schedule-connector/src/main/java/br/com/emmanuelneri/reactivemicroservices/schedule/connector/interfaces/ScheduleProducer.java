package br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.ScheduleEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.ReplyResult;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.Map;

public class ScheduleProducer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleProducer.class);

    static final String SCHEDULE_REQUEST_TOPIC = "ScheduleRequested";

    private final Map<String, String> kafkaProducerConfiguration;

    public ScheduleProducer(final KafkaProducerConfiguration kafkaProducerConfiguration) {
        this.kafkaProducerConfiguration = kafkaProducerConfiguration.createConfig();
    }

    @Override
    public void start() throws Exception {
        final KafkaProducer<String, String> kafkaProducer = KafkaProducer.create(this.vertx, this.kafkaProducerConfiguration);
        this.vertx.eventBus().<JsonObject>consumer(ScheduleEvents.SCHEDULE_VALIDATED.name(), message -> produce(kafkaProducer, message));
    }

    private void produce(final KafkaProducer<String, String> kafkaProducer, final Message<JsonObject> message) {
        try {
            final Schedule schedule = message.body().mapTo(Schedule.class);
            final KafkaProducerRecord<String, String> kafkaProducerRecord =
                    KafkaProducerRecord.create(SCHEDULE_REQUEST_TOPIC, schedule.getCustomer().getDocumentNumber(), message.body().encode());

            kafkaProducer.send(kafkaProducerRecord, result -> {
                if (result.failed()) {
                    LOGGER.error("message send error {0}", kafkaProducerRecord, result.cause());
                    message.reply(ReplyResult.INTERNAL_ERROR.asJson());
                    return;
                }

                LOGGER.info("message produced {0}", kafkaProducerRecord);
                message.reply(ReplyResult.OK.asJson());
            });
        } catch (Exception ex) {
            LOGGER.error("produce error {0}", message, ex);
            message.reply(ReplyResult.INTERNAL_ERROR.asJson());
        }
    }
}
