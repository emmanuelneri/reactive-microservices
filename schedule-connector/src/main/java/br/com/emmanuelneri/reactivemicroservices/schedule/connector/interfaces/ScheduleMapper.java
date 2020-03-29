package br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces;

import br.com.emmanuelneri.reactivemicroservices.mapper.MapperBuilder;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleEndpointSchema;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public final class ScheduleMapper {

    public void map(final Message<String> message,
                    final Handler<AsyncResult<Schedule>> resultHandler) {
        try {
            final ScheduleEndpointSchema schema = Json.decodeValue(message.body(), ScheduleEndpointSchema.class);
            final Schedule schedule = mapToSchedule(schema);
            resultHandler.handle(Future.succeededFuture(schedule));
        } catch (Exception ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }

    private Schedule mapToSchedule(final ScheduleEndpointSchema schema) {
        final Schedule schedule = new Schedule();
        MapperBuilder.INSTANCE.map(schema, schedule);
        return schedule;
    }
}
