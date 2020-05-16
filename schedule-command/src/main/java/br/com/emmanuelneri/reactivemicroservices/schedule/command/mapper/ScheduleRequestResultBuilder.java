package br.com.emmanuelneri.reactivemicroservices.schedule.command.mapper;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.RequestResult;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import java.util.Objects;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

@NoArgsConstructor
public class ScheduleRequestResultBuilder {

    public static final ScheduleRequestResultBuilder INSTANCE = new ScheduleRequestResultBuilder();

    public void success(final ConsumerRecord<String, String> record, final Schedule schedule,
        final Handler<RequestResult> resultHandler) {
        buildReturn(record, true, Json.encode(schedule), null, resultHandler);
    }

    public void fail(final ConsumerRecord<String, String> record, final InvalidMessage invalidMessage,
        final Handler<RequestResult> resultHandler) {
        buildReturn(record, false, null, invalidMessage.getCause(), resultHandler);
    }

    private void buildReturn(final ConsumerRecord<String, String> record, final boolean success, final String value,
        final String description, final Handler<RequestResult> resultHandler) {
        final String requestId = getRequestId(record);

        final RequestResult requestResult = new RequestResult(requestId, success, value, description);
        resultHandler.handle(requestResult);
    }

    private String getRequestId(final ConsumerRecord<String, String> record) {
        final Header header = record.headers().lastHeader(ScheduleSchema.REQUEST_ID_HEADER);
        return Objects.isNull(header) ? null : new String(header.value());
    }
}
