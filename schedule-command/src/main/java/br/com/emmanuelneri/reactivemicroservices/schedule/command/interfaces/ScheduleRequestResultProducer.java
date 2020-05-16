package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.RequestResult;
import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.ReplyResult;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.Map;

public class ScheduleRequestResultProducer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleRequestResultProducer.class);

    public static final String SCHEDULE_PROCESSED_TOPIC = "ScheduleProcessed";
    static final String SCHEDULE_REQUEST_PROCESSED_ADDRESS = ScheduleCommandEvents.SCHEDULE_REQUEST_PROCESSED.getName();

    private final Map<String, String> kafkaProducerConfiguration;

    public ScheduleRequestResultProducer(final KafkaProducerConfiguration kafkaProducerConfiguration) {
        this.kafkaProducerConfiguration = kafkaProducerConfiguration.createConfig();
    }

    @Override
    public void start() throws Exception {
        final KafkaProducer<String, String> kafkaProducer = KafkaProducer.create(this.vertx, this.kafkaProducerConfiguration);
        this.vertx.eventBus().<String>consumer(SCHEDULE_REQUEST_PROCESSED_ADDRESS, message -> produce(kafkaProducer, message));
    }

    private void produce(final KafkaProducer<String, String> kafkaProducer, final Message<String> message) {
        final RequestResult requestResult = Json.decodeValue(message.body(), RequestResult.class);

        final KafkaProducerRecord<String, String> kafkaProducerRecord =
                KafkaProducerRecord.create(SCHEDULE_PROCESSED_TOPIC,
                        requestResult.getRequestId(), Json.encode(requestResult));

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
