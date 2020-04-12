package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import br.com.emmanuelneri.reactivemicroservices.mapper.MapperBuilder;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.Schema;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@AllArgsConstructor(staticName = "create")
final class ScheduleMessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleConsumerVerticle.class);

    private final Vertx vertx;

    void process(final ConsumerRecord<String, String> record, final Promise<Void> promise) {
        try {
            final Schedule schedule = map(record);
            vertx.eventBus().request(ScheduleCommandEvents.SCHEDULE_RECEIVED.getName(), JsonObject.mapFrom(schedule), resultHandler -> {
                if (resultHandler.failed()) {
                    promise.fail(resultHandler.cause());
                    return;
                }

                promise.complete();
                LOGGER.info("message consumed {0}", record);
            });
        } catch (final DecodeException | IllegalArgumentException ex) {
            final JsonObject invalidMessage = JsonObject.mapFrom(InvalidMessage.invalidDecodeValue(record, ex.getMessage()));
            vertx.eventBus().send(ScheduleCommandEvents.INVALID_SCHEDULE_RECEIVED.getName(), invalidMessage);
            LOGGER.error("failed to decode message: {0}", invalidMessage);
            promise.complete();
        } catch (final Exception ex) {
            promise.fail(ex);
        }
    }

    private Schedule map(final ConsumerRecord<String, String> record) {
        final Schema schema = Json.decodeValue(record.value(), Schema.class);

        final Schedule schedule = new Schedule();
        MapperBuilder.INSTANCE.map(schema, schedule);
        return schedule;
    }
}