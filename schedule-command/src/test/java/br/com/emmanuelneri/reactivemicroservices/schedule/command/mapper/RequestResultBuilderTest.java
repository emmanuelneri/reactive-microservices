package br.com.emmanuelneri.reactivemicroservices.schedule.command.mapper;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.mapper.ScheduleRequestResultBuilder;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(VertxUnitRunner.class)
public class RequestResultBuilderTest {

    @Test
    public void shouldReturnResultInSuccessRecord(final TestContext context) {
        final String requestId = UUID.randomUUID().toString();
        final ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "1", "teste");
        record.headers().add(ScheduleSchema.REQUEST_ID_HEADER, requestId.getBytes());

        final Async async = context.async();
        ScheduleRequestResultBuilder.INSTANCE.success(record, new Schedule(), result -> {
            context.assertTrue(result.isSuccess());
            context.assertNull(result.getDescription());
            context.assertEquals(requestId, result.getRequestId());
            async.complete();
        });
    }

    @Test
    public void shouldReturnResultInFailRecord(final TestContext context) {
        final String requestId = UUID.randomUUID().toString();
        final ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "1", "teste");
        record.headers().add(ScheduleSchema.REQUEST_ID_HEADER, requestId.getBytes());

        final InvalidMessage invalidMessage = InvalidMessage.invalidBusinessValidation(record, "date is required");

        final Async async = context.async();
        ScheduleRequestResultBuilder.INSTANCE.fail(record, invalidMessage, result -> {
            context.assertFalse(result.isSuccess());
            context.assertEquals("date is required", result.getDescription());
            context.assertEquals(requestId, result.getRequestId());
            async.complete();
        });
    }
}