package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ScheduleMessageProcessorTest {

    private Vertx vertx;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void after() {
        this.vertx.close();
    }

    @Test
    public void shouldSucessProcess(final TestContext context) {
        this.vertx.eventBus().consumer(ScheduleCommandEvents.SCHEDULE_RECEIVED.getName(), messageResult -> messageResult.reply("ok"));

        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
        final String messageValue = "{\"dateTime\":\"2020-04-14T20:34:56\",\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}";

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
    public void shouldReturnFailWithInvalidSchema(final TestContext context) {
        this.vertx.eventBus().consumer(ScheduleCommandEvents.SCHEDULE_RECEIVED.getName(), messageResult -> messageResult.reply("ok"));

        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
        final String messageValue = "{\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}";

        final Promise<Void> promise = Promise.promise();
        scheduleMessageProcessor.process(new ConsumerRecord<>("topic", 0, 0, "123", messageValue), promise);

        final Async async = context.async();
        promise.future().setHandler(resultHandler -> {
            context.assertTrue(resultHandler.failed());
            context.assertEquals("dateTime is required", resultHandler.cause().getMessage());
            async.complete();
        });
    }

    @Test
    public void shouldNotReturnExceptionWithInvalidJsonMessageValue(final TestContext context) {
        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
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