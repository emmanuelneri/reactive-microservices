package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.exceptions.ValidationException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
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
        ScheduleMapper.INSTANCE.map(record, mapResultHandler -> {
            if (mapResultHandler.failed()) {
                failedHandler(record, promise, mapResultHandler);
                return;
            }

            final Schedule schedule = mapResultHandler.result();
            schedule.validate(validateResultHandler -> {
                if (validateResultHandler.failed()) {
                    promise.fail(validateResultHandler.cause());
                    return;
                }

                vertx.eventBus().request(ScheduleCommandEvents.SCHEDULE_RECEIVED.getName(), JsonObject.mapFrom(schedule), resultHandler -> {
                    if (resultHandler.failed()) {
                        failedHandler(record, promise, resultHandler);
                        return;
                    }

                    promise.complete();
                    LOGGER.info("message consumed {0}", record);
                });
            });
        });
    }

    private void failedHandler(final ConsumerRecord<String, String> record, final Promise<Void> promise, final AsyncResult<?> resultHandler) {
        if (resultHandler.cause() instanceof ValidationException) {
            final InvalidMessage invalidMessage = ((ValidationException) resultHandler.cause()).buildErrorMessage(record);
            vertx.eventBus().publish(ScheduleCommandEvents.INVALID_SCHEDULE_RECEIVED.getName(), JsonObject.mapFrom(invalidMessage));
            promise.complete();
        } else {
            promise.fail(resultHandler.cause());
        }
    }
}