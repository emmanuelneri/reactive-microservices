package br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces;

import br.com.emmanuelneri.reactivemicroservices.exception.ValidationException;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.domain.Events;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.ReplyResult;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ScheduleProcessor extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleProcessor.class);
    private ScheduleMapper scheduleMapper;

    @Override
    public void start() throws Exception {
        this.scheduleMapper = ScheduleMapper.create();
        this.vertx.eventBus().consumer(Events.SCHEDULE_RECEIVED.name(), this::processSchema);
    }

    private void processSchema(final Message<String> message) {
        scheduleMapper.map(message, mapperAsyncResult -> {
            if (mapperAsyncResult.failed()) {
                LOGGER.error("conversion failed", mapperAsyncResult.cause());
                message.reply(ReplyResult.error(String.format("Invalid schema: %s", mapperAsyncResult.cause().getMessage())).asJson());
                return;
            }

            final Schedule schedule = mapperAsyncResult.result();
            validate(schedule, validateAsyncResult -> {
                if (validateAsyncResult.failed()) {
                    LOGGER.error("invalid schema", mapperAsyncResult.cause());
                    message.reply(ReplyResult.error(validateAsyncResult.cause().getMessage()).asJson());
                    return;
                }

                sendToProduce(schedule, sendAsyncResult -> {
                    if (sendAsyncResult.failed()) {
                        LOGGER.error("schedule producer error", sendAsyncResult.cause());
                        message.reply(ReplyResult.error("internal error").asJson());
                        return;
                    }

                    message.reply(ReplyResult.OK.asJson());
                });
            });
        });
    }

    private void validate(final Schedule schedule, final Handler<AsyncResult<Void>> resultHandler) {
        try {
            schedule.validate();
            resultHandler.handle(Future.succeededFuture());
        } catch (final ValidationException vex) {
            resultHandler.handle(Future.failedFuture(vex));
        }
    }

    private void sendToProduce(final Schedule schedule, final Handler<AsyncResult<Void>> resultHandler) {
        this.vertx.eventBus().request(Events.SCHEDULE_VALIDATED.name(), JsonObject.mapFrom(schedule), requestAsyncResult -> {
            if (requestAsyncResult.failed()) {
                resultHandler.handle(Future.failedFuture(requestAsyncResult.cause()));
                return;
            }

            resultHandler.handle(Future.succeededFuture());
        });
    }

}
