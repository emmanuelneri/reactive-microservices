package br.com.emmanuelneri.blueprint.schedule.connector.interfaces;

import br.com.emmanuelneri.blueprint.mapper.MapperBuilder;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.Schedule;
import br.com.emmanuelneri.schedule.schema.ScheduleEndpointSchema;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public final class ScheduleMapper {

    public void map(final Message<String> message,
                    final Handler<Schedule> successHandler,
                    final Handler<Throwable> errorHandler) {
        try {
            final ScheduleEndpointSchema schema = Json.decodeValue(message.body(), ScheduleEndpointSchema.class);
            successHandler.handle(map(schema));
        } catch (Exception ex) {
            errorHandler.handle(ex);
        }
    }

    private Schedule map(final ScheduleEndpointSchema schema) {
        final Schedule schedule = new Schedule();
        MapperBuilder.INSTANCE.map(schema, schedule);
        return schedule;
    }

}
