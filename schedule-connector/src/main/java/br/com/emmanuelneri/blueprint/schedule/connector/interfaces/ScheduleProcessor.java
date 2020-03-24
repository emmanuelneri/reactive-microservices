package br.com.emmanuelneri.blueprint.schedule.connector.interfaces;

import br.com.emmanuelneri.blueprint.exception.ValidationException;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.Events;
import br.com.emmanuelneri.blueprint.vertx.eventbus.ReplyResult;
import io.vertx.core.AbstractVerticle;
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
        scheduleMapper.map(message, schedule -> {
            try {
                schedule.validate();
                this.vertx.eventBus().request(Events.SCHEDULE_VALIDATED.name(), JsonObject.mapFrom(schedule), async -> {
                    if (async.failed()) {
                        LOGGER.error("schedule validated request error", async.cause());
                        message.reply(ReplyResult.error("internal error").asJson());
                        return;
                    }

                    message.reply(ReplyResult.OK.asJson());
                });
            } catch (ValidationException vex) {
                message.reply(ReplyResult.error(vex.getMessage()).asJson());
            }
        }, error -> {
            LOGGER.error("conversion failed", error);
            message.reply(ReplyResult.error(String.format("Invalid schema: %s", error.getMessage())).asJson());
        });
    }
}
