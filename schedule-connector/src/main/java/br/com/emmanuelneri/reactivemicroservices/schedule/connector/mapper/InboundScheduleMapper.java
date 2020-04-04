package br.com.emmanuelneri.reactivemicroservices.schedule.connector.mapper;

import br.com.emmanuelneri.reactivemicroservices.exception.InvalidSchemaException;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.domain.Schedule;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public final class InboundScheduleMapper {

    public void map(final Message<String> message,
                    final Handler<AsyncResult<Schedule>> resultHandler) {
        try {
            final Schedule schedule = Json.decodeValue(message.body(), Schedule.class);
            resultHandler.handle(Future.succeededFuture(schedule));
        } catch (final DecodeException dex) {
            resultHandler.handle(Future.failedFuture(new InvalidSchemaException(message.body())));
        } catch (final Exception ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }
}
