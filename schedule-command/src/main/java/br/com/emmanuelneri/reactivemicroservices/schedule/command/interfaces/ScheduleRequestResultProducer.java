package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleRequestResult;
import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.ReplyResult;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.Map;

public class ScheduleRequestResultProducer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleRequestResultProducer.class);

    static final String SCHEDULE_REQUEST_TOPIC = "ScheduleRequestResultProcessed";
    static final String SCHEDULE_RETURN_REQUEST_PROCESSED_ADDRESS = ScheduleCommandEvents.SCHEDULE_RETURN_REQUEST_PROCESSED.getName();


    private final Map<String, String> kafkaProducerConfiguration;

    public ScheduleRequestResultProducer(final KafkaProducerConfiguration kafkaProducerConfiguration) {
        this.kafkaProducerConfiguration = kafkaProducerConfiguration.createConfig();
    }

    @Override
    public void start() throws Exception {
        final KafkaProducer<String, String> kafkaProducer = KafkaProducer.create(this.vertx, this.kafkaProducerConfiguration);
        this.vertx.eventBus().<JsonObject>consumer(SCHEDULE_RETURN_REQUEST_PROCESSED_ADDRESS, message -> produce(kafkaProducer, message));
    }

    private void produce(final KafkaProducer<String, String> kafkaProducer, final Message<JsonObject> message) {
        final ScheduleRequestResult scheduleRequestResult = message.body().mapTo(ScheduleRequestResult.class);

        final KafkaProducerRecord<String, String> kafkaProducerRecord =
                KafkaProducerRecord.create(SCHEDULE_REQUEST_TOPIC,
                        scheduleRequestResult.getRequestId(), Json.encode(scheduleRequestResult));

        kafkaProducer.send(kafkaProducerRecord, result -> {
            if (result.failed()) {
                LOGGER.error("message send error {0}", kafkaProducerRecord, result.cause());
                message.reply(ReplyResult.INTERNAL_ERROR.asJson());
                return;
            }

            LOGGER.info("message produced {0}", kafkaProducerRecord);
        });
    }
}
