package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)

public class ScheduleMapperTest {

    @Test
    public void shouldSuccessWitValidJsonMessageValue(final TestContext context) {
        final String messageValue = "{\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}";

        final Async async = context.async();

        ScheduleMapper.INSTANCE.map(new ConsumerRecord<>("topic", 0, 0, "123", messageValue), mapResultHandler -> {
            context.assertTrue(mapResultHandler.succeeded());
            context.assertNull(mapResultHandler.cause());
            async.complete();
        });
    }

    @Test
    public void shouldFailWithNoJsonMessageValue(final TestContext context) {
        final String messageValue = "teste";

        final Async async = context.async();
        ScheduleMapper.INSTANCE.map(new ConsumerRecord<>("topic", 0, 0, "123", messageValue), mapResultHandler -> {
            context.assertTrue(mapResultHandler.failed());
            context.assertNotNull(mapResultHandler.cause());
            async.complete();
        });
    }

    @Test
    public void shouldFailWithInvalidJsonMessageValue(final TestContext context) {
        final String messageValue = "{\"dateTime\":\"12-04-2020\",\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}";

        final Async async = context.async();

        ScheduleMapper.INSTANCE.map(new ConsumerRecord<>("topic", 0, 0, "123", messageValue), mapResultHandler -> {
            context.assertTrue(mapResultHandler.failed());
            context.assertNotNull(mapResultHandler.cause());
            async.complete();
        });
    }
}