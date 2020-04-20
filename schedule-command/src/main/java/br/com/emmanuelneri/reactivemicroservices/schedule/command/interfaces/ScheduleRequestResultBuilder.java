package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleRequestResult;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import io.vertx.core.Handler;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.util.Objects;

@NoArgsConstructor
class ScheduleRequestResultBuilder {

    static final ScheduleRequestResultBuilder INSTANCE = new ScheduleRequestResultBuilder();

    public void success(final ConsumerRecord<String, String> record, final Handler<ScheduleRequestResult> resultHandler) {
        buildReturn(record, true, null, resultHandler);
    }

    public void fail(final ConsumerRecord<String, String> record, final InvalidMessage invalidMessage, final Handler<ScheduleRequestResult> resultHandler) {
        buildReturn(record, false, invalidMessage.getCause(), resultHandler);
    }

    private void buildReturn(final ConsumerRecord<String, String> record, final boolean success, final String description, final Handler<ScheduleRequestResult> resultHandler) {
        final String requestId = getRequestId(record);

        final ScheduleRequestResult scheduleRequestResult = new ScheduleRequestResult(requestId, success, description);
        resultHandler.handle(scheduleRequestResult);
    }

    private String getRequestId(final ConsumerRecord<String, String> record) {
        final Header header = record.headers().lastHeader(ScheduleSchema.REQUEST_ID_HEADER);
        return Objects.isNull(header) ? null : new String(header.value());
    }
}
