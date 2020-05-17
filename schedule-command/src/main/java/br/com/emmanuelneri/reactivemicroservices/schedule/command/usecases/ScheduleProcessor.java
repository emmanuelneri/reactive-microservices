package br.com.emmanuelneri.reactivemicroservices.schedule.command.usecases;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.exceptions.ValidationException;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.mapper.ScheduleMapper;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.mapper.ScheduleRequestResultBuilder;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.RequestResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@AllArgsConstructor(staticName = "create")
public final class ScheduleProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleProcessor.class);
    static final String SCHEDULE_RECEIVED_ADDRESS = ScheduleCommandEvents.SCHEDULE_RECEIVED.getName();
    static final String INVALID_SCHEDULE_RECEIVED_ADDRESS = ScheduleCommandEvents.INVALID_SCHEDULE_RECEIVED.getName();
    static final String SCHEDULE_REQUEST_PROCESSED_ADDRESS = ScheduleCommandEvents.SCHEDULE_REQUEST_PROCESSED.getName();

    private final Vertx vertx;

    public void process(final ConsumerRecord<String, String> record, final Promise<Void> promise) {
        ScheduleMapper.INSTANCE.map(record, mapResultHandler -> {
            if (mapResultHandler.failed()) {
                failedHandler(record, promise, mapResultHandler);
                return;
            }

            final Schedule schedule = mapResultHandler.result();
            schedule.validate(validateResultHandler -> {
                if (validateResultHandler.failed()) {
                    failedHandler(record, promise, validateResultHandler);
                    return;
                }

                this.vertx.eventBus().request(SCHEDULE_RECEIVED_ADDRESS, Json.encode(schedule), resultHandler -> {
                    if (resultHandler.failed()) {
                        failedHandler(record, promise, resultHandler);
                        return;
                    }

                    ScheduleRequestResultBuilder.INSTANCE.success(record, schedule, requestResultHandler());
                    promise.complete();
                    LOGGER.info("message consumed {0}", record);
                });
            });
        });
    }

    private void failedHandler(final ConsumerRecord<String, String> record, final Promise<Void> promise,
        final AsyncResult<?> resultHandler) {
        InvalidMessage invalidMessage;

        if (resultHandler.cause() instanceof ValidationException) {
            invalidMessage = ((ValidationException)resultHandler.cause()).buildErrorMessage(record);
            this.vertx.eventBus().publish(INVALID_SCHEDULE_RECEIVED_ADDRESS, Json.encode(invalidMessage));
            promise.complete();
        }
        else {
            invalidMessage = InvalidMessage.unexpectedFailure(record, resultHandler.cause());
            promise.fail(resultHandler.cause());
        }

        ScheduleRequestResultBuilder.INSTANCE.fail(record, invalidMessage, requestResultHandler());
    }

    private Handler<RequestResult> requestResultHandler() {
        return requestResult -> this.vertx.eventBus().publish(SCHEDULE_REQUEST_PROCESSED_ADDRESS, Json.encode(requestResult));
    }
}