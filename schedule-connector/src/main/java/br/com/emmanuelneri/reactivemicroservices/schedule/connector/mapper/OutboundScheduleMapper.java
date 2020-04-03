package br.com.emmanuelneri.reactivemicroservices.schedule.connector.mapper;

import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public final class OutboundScheduleMapper {

    public void map(final JsonObject jsonObject, final Handler<AsyncResult<ScheduleSchema>> resultHandler) {
        try {
            final ScheduleSchema schema = jsonObject.mapTo(ScheduleSchema.class);
            resultHandler.handle(Future.succeededFuture(schema));
        } catch (final Exception ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }
}
