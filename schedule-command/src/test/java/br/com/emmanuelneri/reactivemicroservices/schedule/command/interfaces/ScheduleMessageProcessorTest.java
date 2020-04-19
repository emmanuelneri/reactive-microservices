package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessageReason;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleMessageProcessor.INVALID_SCHEDULE_RECEIVED_ADDRESS;
import static br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleMessageProcessor.SCHEDULE_RECEIVED_ADDRESS;

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
        this.vertx.eventBus().consumer(SCHEDULE_RECEIVED_ADDRESS, messageResult -> messageResult.reply("ok"));

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
        final Async async = context.async();
        this.vertx.eventBus().<JsonObject>consumer(INVALID_SCHEDULE_RECEIVED_ADDRESS, messageResult -> {
            final InvalidMessage invalidMessage = messageResult.body().mapTo(InvalidMessage.class);
            context.assertEquals(InvalidMessageReason.BUSINESS_VALIDATION_FAILURE, invalidMessage.getReason());
            context.assertEquals("dateTime is required", invalidMessage.getCause());
            async.complete();
        });

        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
        final String messageValue = "{\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}";

        final Promise<Void> promise = Promise.promise();
        scheduleMessageProcessor.process(new ConsumerRecord<>("topic", 0, 0, "123", messageValue), promise);

        promise.future().setHandler(resultHandler -> {
            if (resultHandler.failed()) {
                context.fail(resultHandler.cause());
                return;
            }

            context.assertTrue(resultHandler.succeeded());
        });
    }

    @Test
    public void shouldNotReturnExceptionWithInvalidJsonMessageValue(final TestContext context) {
        final Async async = context.async();
        this.vertx.eventBus().<JsonObject>consumer(INVALID_SCHEDULE_RECEIVED_ADDRESS, messageResult -> {
            final InvalidMessage invalidMessage = messageResult.body().mapTo(InvalidMessage.class);
            context.assertEquals(InvalidMessageReason.VALUE_DECODE_FAILURE, invalidMessage.getReason());
            context.assertNotNull(invalidMessage.getCause());
            async.complete();
        });

        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
        final String messageValue = "{\"dateTime\":\"12-04-2020\",\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}";

        final Promise<Void> promise = Promise.promise();
        scheduleMessageProcessor.process(new ConsumerRecord<>("topic", 0, 0, "123", messageValue), promise);

        promise.future().setHandler(resultHandler -> {
            if (resultHandler.failed()) {
                context.fail(resultHandler.cause());
                return;
            }

            context.assertTrue(resultHandler.succeeded());
        });
    }
}