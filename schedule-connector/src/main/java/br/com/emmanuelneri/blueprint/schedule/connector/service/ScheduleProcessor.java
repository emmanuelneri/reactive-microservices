package br.com.emmanuelneri.blueprint.schedule.connector.service;

import br.com.emmanuelneri.blueprint.exception.ValidationException;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.Events;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.ProcessorResult;
import br.com.emmanuelneri.blueprint.schedule.connector.interfaces.ScheduleMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
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

    private void processSchema(final Message<Object> message) {
        scheduleMapper.map(message, schedule -> {
            try {
                schedule.validate();
                message.reply(Json.encode(ProcessorResult.OK));
            } catch (ValidationException vex) {
                message.reply(Json.encode(ProcessorResult.error(vex.getMessage())));
            }
        }, error -> {
            LOGGER.error("conversion failed", error);
            message.reply(Json.encode(ProcessorResult.error(String.format("Invalid schema: %s", error.getMessage()))));
        });
    }
}
