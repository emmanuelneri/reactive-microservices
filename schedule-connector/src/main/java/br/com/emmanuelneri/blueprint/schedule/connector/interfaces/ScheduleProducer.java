package br.com.emmanuelneri.blueprint.schedule.connector.interfaces;

import br.com.emmanuelneri.blueprint.kafka.KafkaProducerConfiguration;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.Events;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.ProcessorResult;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.Schedule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
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
        this.vertx.eventBus().consumer(Events.SCHEDULE_VALIDATED.name(), message -> produce(kafkaProducer, message));
    }

    private void produce(final KafkaProducer<String, String> kafkaProducer, final Message<Object> message) {
        try {
            final Schedule schedule = Json.decodeValue(message.body().toString(), Schedule.class);

            final KafkaProducerRecord<String, String> kafkaProducerRecord =
                    KafkaProducerRecord.create(SCHEDULE_REQUEST_TOPIC, schedule.createTopicKey(), Json.encode(schedule));

            kafkaProducer.send(kafkaProducerRecord, result -> {
                if (result.failed()) {
                    LOGGER.error("message send error {0}", kafkaProducerRecord, result.cause());
                    message.reply(ProcessorResult.INTERNAL_ERROR_AS_JSON);
                    return;
                }

                LOGGER.info("message produced {0}", kafkaProducerRecord);
                message.reply(ProcessorResult.OK_AS_JSON);
            });
        } catch (Exception ex) {
            LOGGER.error("produce error {0}", message, ex);
            message.reply(ProcessorResult.INTERNAL_ERROR_AS_JSON);
        }
    }
}
