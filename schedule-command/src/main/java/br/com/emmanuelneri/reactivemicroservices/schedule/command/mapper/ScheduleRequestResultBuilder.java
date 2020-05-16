package br.com.emmanuelneri.reactivemicroservices.schedule.command.mapper;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.RequestResult;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import io.vertx.core.Handler;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.util.Objects;

@NoArgsConstructor
public class ScheduleRequestResultBuilder {

    public static final ScheduleRequestResultBuilder INSTANCE = new ScheduleRequestResultBuilder();

    public void success(final ConsumerRecord<String, String> record, final Handler<RequestResult> resultHandler) {
        buildReturn(record, true, null, resultHandler);
    }

    public void fail(final ConsumerRecord<String, String> record, final InvalidMessage invalidMessage, final Handler<RequestResult> resultHandler) {
        buildReturn(record, false, invalidMessage.getCause(), resultHandler);
    }

    private void buildReturn(final ConsumerRecord<String, String> record, final boolean success, final String description, final Handler<RequestResult> resultHandler) {
        final String requestId = getRequestId(record);

        final RequestResult requestResult = new RequestResult(requestId, success, description);
        resultHandler.handle(requestResult);
    }

    private String getRequestId(final ConsumerRecord<String, String> record) {
        final Header header = record.headers().lastHeader(ScheduleSchema.REQUEST_ID_HEADER);
        return Objects.isNull(header) ? null : new String(header.value());
    }
}
