package br.com.emmanuelneri.reactivemicroservices.schedule.connector.mapper;

import br.com.emmanuelneri.reactivemicroservices.mapper.MapperBuilder;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.schema.ScheduleOutbound;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public final class OutboundScheduleMapper {

    public void map(final JsonObject scheduleJsonObject,
                    final Handler<AsyncResult<ScheduleOutbound>> resultHandler) {
        try {
            final Schedule schedule = scheduleJsonObject.mapTo(Schedule.class);
            final ScheduleSchema schema = mapSchema(schedule);
            resultHandler.handle(Future.succeededFuture(new ScheduleOutbound(schema, schedule.getRequestId())));
        } catch (final Exception ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }

    private ScheduleSchema mapSchema(final Schedule schedule) {
        final ScheduleSchema schema = new ScheduleSchema();
        MapperBuilder.INSTANCE.map(schedule, schema);
        return schema;
    }

}
