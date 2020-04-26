package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessageReason;
import br.com.emmanuelneri.reactivemicroservices.mapper.JsonConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleRequestResult;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
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

import java.util.UUID;

import static br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleMessageProcessor.INVALID_SCHEDULE_RECEIVED_ADDRESS;
import static br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleMessageProcessor.SCHEDULE_RECEIVED_ADDRESS;
import static br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleMessageProcessor.SCHEDULE_RETURN_REQUEST_PROCESSED_ADDRESS;

@RunWith(VertxUnitRunner.class)
public class ScheduleMessageProcessorTest {

    private Vertx vertx;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
        JsonConfiguration.setUpDefault();
    }

    @After
    public void after() {
        this.vertx.close();
    }

    @Test
    public void shouldSucessProcess(final TestContext context) {
        this.vertx.eventBus().consumer(SCHEDULE_RECEIVED_ADDRESS, messageResult -> messageResult.reply("ok"));

        final Async async = context.async();
        this.vertx.eventBus().<JsonObject>consumer(SCHEDULE_RETURN_REQUEST_PROCESSED_ADDRESS, messageResult -> {
            final ScheduleRequestResult scheduleRequestResult = messageResult.body().mapTo(ScheduleRequestResult.class);
            context.assertTrue(scheduleRequestResult.isSuccess(), scheduleRequestResult.toString());
            context.assertNotNull(scheduleRequestResult.getRequestId());
            async.complete();
        });

        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
        final String messageValue = "{\"dateTime\":\"2020-05-09T12:14:50.786\",\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}";

        final Promise<Void> promise = Promise.promise();
        final ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "123", messageValue);
        record.headers().add(ScheduleSchema.REQUEST_ID_HEADER, UUID.randomUUID().toString().getBytes());
        scheduleMessageProcessor.process(record, promise);
        promise.future().setHandler(resultHandler -> {
            if (resultHandler.failed()) {
                context.fail(resultHandler.cause());
                return;
            }

            context.assertTrue(resultHandler.succeeded());
        });
    }

    @Test
    public void shouldReturnFailWithInvalidSchema(final TestContext context) {
        final Async asyncErrorNotification = context.async();
        this.vertx.eventBus().<JsonObject>consumer(INVALID_SCHEDULE_RECEIVED_ADDRESS, messageResult -> {
            final InvalidMessage invalidMessage = messageResult.body().mapTo(InvalidMessage.class);
            context.assertEquals(InvalidMessageReason.BUSINESS_VALIDATION_FAILURE, invalidMessage.getReason());
            context.assertEquals("dateTime is required", invalidMessage.getCause());
            asyncErrorNotification.complete();
        });

        final Async asyncReturnRequest = context.async();
        this.vertx.eventBus().<JsonObject>consumer(SCHEDULE_RETURN_REQUEST_PROCESSED_ADDRESS, messageResult -> {
            final ScheduleRequestResult scheduleRequestResult = messageResult.body().mapTo(ScheduleRequestResult.class);
            context.assertFalse(scheduleRequestResult.isSuccess(), scheduleRequestResult.toString());
            context.assertNotNull(scheduleRequestResult.getRequestId());
            context.assertEquals("dateTime is required", scheduleRequestResult.getDescription());
            asyncReturnRequest.complete();
        });

        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
        final String messageValue = "{\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}";

        final Promise<Void> promise = Promise.promise();
        final ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "123", messageValue);
        record.headers().add(ScheduleSchema.REQUEST_ID_HEADER, UUID.randomUUID().toString().getBytes());
        scheduleMessageProcessor.process(record, promise);

        promise.future().setHandler(resultHandler -> {
            if (resultHandler.failed()) {
                context.fail(resultHandler.cause());
                return;
            }

            context.assertTrue(resultHandler.succeeded());
        });

        asyncErrorNotification.awaitSuccess();
        asyncReturnRequest.awaitSuccess();
    }

    @Test
    public void shouldNotReturnExceptionWithInvalidJsonMessageValue(final TestContext context) {
        final Async asyncErrorNotification = context.async();
        this.vertx.eventBus().<JsonObject>consumer(INVALID_SCHEDULE_RECEIVED_ADDRESS, messageResult -> {
            final InvalidMessage invalidMessage = messageResult.body().mapTo(InvalidMessage.class);
            context.assertEquals(InvalidMessageReason.VALUE_DECODE_FAILURE, invalidMessage.getReason());
            context.assertNotNull(invalidMessage.getCause());
            asyncErrorNotification.complete();
        });

        final Async asyncReturnRequest = context.async();
        this.vertx.eventBus().<JsonObject>consumer(SCHEDULE_RETURN_REQUEST_PROCESSED_ADDRESS, messageResult -> {
            final ScheduleRequestResult scheduleRequestResult = messageResult.body().mapTo(ScheduleRequestResult.class);
            context.assertFalse(scheduleRequestResult.isSuccess(), scheduleRequestResult.toString());
            context.assertNotNull(scheduleRequestResult.getRequestId());
            context.assertNotNull(scheduleRequestResult.getDescription());
            asyncReturnRequest.complete();
        });

        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
        final String messageValue = "{\"dateTime\":\"12-04-2020\",\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Complete Test\"}";

        final Promise<Void> promise = Promise.promise();
        final ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "123", messageValue);
        record.headers().add(ScheduleSchema.REQUEST_ID_HEADER, UUID.randomUUID().toString().getBytes());
        scheduleMessageProcessor.process(record, promise);

        promise.future().setHandler(resultHandler -> {
            if (resultHandler.failed()) {
                context.fail(resultHandler.cause());
                return;
            }

            context.assertTrue(resultHandler.succeeded());
        });
    }
}