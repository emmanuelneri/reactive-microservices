package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ScheduleMessageProcessorTest {

    @Test
    public void shouldNotReturnExceptionWithNoJsonMessageValue(final TestContext context) {
        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(Vertx.vertx());
        final String messageValue = "teste";

        final Promise<Void> promise = Promise.promise();
        scheduleMessageProcessor.process(new ConsumerRecord<>("topic", 0, 0, "123", messageValue), promise);

        final Async async = context.async();
        promise.future().setHandler(resultHandler -> {
            if (resultHandler.failed()) {
                context.fail(resultHandler.cause());
                return;
            }

            async.complete();
        });
    }

    @Test
    public void shouldNotReturnExceptionWithInvalidJsonMessageValue(final TestContext context) {
        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(Vertx.vertx());
        final String messageValue = "{\"dateTime\":\"12-04-2020\",\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}";

        final Promise<Void> promise = Promise.promise();
        scheduleMessageProcessor.process(new ConsumerRecord<>("topic", 0, 0, "123", messageValue), promise);

        final Async async = context.async();
        promise.future().setHandler(resultHandler -> {
            if (resultHandler.failed()) {
                context.fail(resultHandler.cause());
                return;
            }

            async.complete();
        });
    }
}