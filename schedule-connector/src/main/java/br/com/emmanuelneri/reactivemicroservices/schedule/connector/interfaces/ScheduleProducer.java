package br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.ScheduleConnectorEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.mapper.OutboundScheduleMapper;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.schema.ScheduleOutbound;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
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

public class ScheduleProducer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleProducer.class);

    static final String SCHEDULE_REQUEST_TOPIC = "ScheduleRequested";

    private final Map<String, String> kafkaProducerConfiguration;
    private final OutboundScheduleMapper outboundScheduleMapper;

    public ScheduleProducer(final KafkaProducerConfiguration kafkaProducerConfiguration) {
        this.kafkaProducerConfiguration = kafkaProducerConfiguration.createConfig();
        this.outboundScheduleMapper = OutboundScheduleMapper.create();
    }

    @Override
    public void start() throws Exception {
        final KafkaProducer<String, String> kafkaProducer = KafkaProducer.create(this.vertx, this.kafkaProducerConfiguration);
        this.vertx.eventBus().<JsonObject>consumer(ScheduleConnectorEvents.SCHEDULE_VALIDATED.name(), message -> produce(kafkaProducer, message));
    }

    private void produce(final KafkaProducer<String, String> kafkaProducer, final Message<JsonObject> message) {
        try {
            outboundScheduleMapper.map(message.body(), schemaMapperAsyncResult -> {
                if (schemaMapperAsyncResult.failed()) {
                    LOGGER.error("map to schema failed: {0}", message, schemaMapperAsyncResult.cause());
                    message.reply(ReplyResult.INTERNAL_ERROR.asJson());
                    return;
                }

                final ScheduleOutbound scheduleOutbound = schemaMapperAsyncResult.result();
                final KafkaProducerRecord<String, String> kafkaProducerRecord =
                        KafkaProducerRecord.create(SCHEDULE_REQUEST_TOPIC,
                                scheduleOutbound.getKey(), Json.encode(scheduleOutbound.getSchema()));

                final String requestId = scheduleOutbound.getRequestId().toString();
                kafkaProducerRecord.addHeader(ScheduleSchema.REQUEST_ID_HEADER, requestId);

                kafkaProducer.exceptionHandler(throwable -> {
                    LOGGER.error("Kafka exception handler: {0}", kafkaProducerRecord, throwable);
                    message.reply(ReplyResult.INTERNAL_ERROR.asJson());
                });

                kafkaProducer.send(kafkaProducerRecord, result -> {
                    if (result.failed()) {
                        LOGGER.error("message send error {0}", kafkaProducerRecord, result.cause());
                        message.reply(ReplyResult.INTERNAL_ERROR.asJson());
                        return;
                    }

                    LOGGER.info("message produced {0}", kafkaProducerRecord);
                    message.reply(ReplyResult.ok(requestId).asJson());
                });
            });

        } catch (Exception ex) {
            LOGGER.error("produce error {0}", message, ex);
            message.reply(ReplyResult.INTERNAL_ERROR.asJson());
        }
    }
}
