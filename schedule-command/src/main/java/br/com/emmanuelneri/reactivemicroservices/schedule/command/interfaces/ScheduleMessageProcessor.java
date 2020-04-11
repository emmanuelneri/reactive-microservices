package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@AllArgsConstructor(staticName = "create")
final class ScheduleMessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleConsumerVerticle.class);

    private final Vertx vertx;

    void process(final ConsumerRecord<String, String> record) {
        try {
            final JsonObject schema = mapValue(record);
            validate(schema);
            vertx.eventBus().send(ScheduleCommandEvents.SCHEDULE_RECEIVED.getName(), schema);
            LOGGER.info("message consumed {0}", record);
        } catch (final DecodeException | IllegalArgumentException ex) {
            final JsonObject invalidMessage = JsonObject.mapFrom(InvalidMessage.invalidDecodeValue(record, ex.getMessage()));
            vertx.eventBus().send(ScheduleCommandEvents.INVALID_SCHEDULE_RECEIVED.getName(), invalidMessage);
            LOGGER.error("failed to decode message: {0}", invalidMessage);
        }
    }

    private JsonObject mapValue(final ConsumerRecord<String, String> record) {
        return new JsonObject(record.value());
    }

    private void validate(final JsonObject schema) {
        schema.mapTo(ScheduleSchema.class);
    }
}