package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessageReason;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.CustomerSchema;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.RequestResult;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import br.com.emmanuelneri.reactivemicroservices.vertx.core.VertxBuilder;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.UUID;

import static br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleMessageProcessor.INVALID_SCHEDULE_RECEIVED_ADDRESS;
import static br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleMessageProcessor.SCHEDULE_RECEIVED_ADDRESS;
import static br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleMessageProcessor.SCHEDULE_RETURN_REQUEST_PROCESSED_ADDRESS;

@RunWith(VertxUnitRunner.class)
public class ScheduleMessageProcessorTest {

    private Vertx vertx;

    @Before
    public void before() {
        this.vertx = VertxBuilder.createAndConfigure();
    }

    @After
    public void after() {
        this.vertx.close();
    }

    @Test
    public void shouldSuccessProcess(final TestContext context) {
        System.out.println("--------------");
        System.out.println(Json.mapper.getRegisteredModuleIds().toString());
        System.out.println(DatabindCodec.mapper().getRegisteredModuleIds().toString());
        System.out.println("--------------");
        this.vertx.eventBus().consumer(SCHEDULE_RECEIVED_ADDRESS, messageResult -> messageResult.reply("ok"));

        final String requestId = UUID.randomUUID().toString();
        final Async async = context.async();
        this.vertx.eventBus().<String>consumer(SCHEDULE_RETURN_REQUEST_PROCESSED_ADDRESS, messageResult -> {
            final RequestResult requestResult = Json.decodeValue(messageResult.body(), RequestResult.class);
            if (requestId.equals(requestResult.getRequestId())) {
                context.assertTrue(requestResult.isSuccess(), requestResult.toString());
                async.complete();
            }
        });

        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
        final CustomerSchema customerSchema = new CustomerSchema();
        customerSchema.setDocumentNumber("948948393849");
        customerSchema.setEmail("teste@gmail.com");
        customerSchema.setPhone("4499099493");
        customerSchema.setName("Customer 1");

        final ScheduleSchema schema = new ScheduleSchema();
        schema.setDescription("Success Test");
        schema.setDateTime(LocalDateTime.now());
        schema.setCustomer(customerSchema);

        final String messageValue = Json.encode(schema);

        final Promise<Void> promise = Promise.promise();
        final ConsumerRecord<String, String> record = new ConsumerRecord<>("success.topic", 0, 0, "1", messageValue);
        record.headers().add(ScheduleSchema.REQUEST_ID_HEADER, requestId.getBytes());
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
        this.vertx.eventBus().<String>consumer(INVALID_SCHEDULE_RECEIVED_ADDRESS, messageResult -> {
            final InvalidMessage invalidMessage = Json.decodeValue(messageResult.body(), InvalidMessage.class);
            context.assertEquals(InvalidMessageReason.BUSINESS_VALIDATION_FAILURE, invalidMessage.getReason());
            context.assertEquals("dateTime is required", invalidMessage.getCause());
            asyncErrorNotification.complete();
        });

        final String requestId = UUID.randomUUID().toString();
        final Async asyncReturnRequest = context.async();
        this.vertx.eventBus().<String>consumer(SCHEDULE_RETURN_REQUEST_PROCESSED_ADDRESS, messageResult -> {
            final RequestResult requestResult = Json.decodeValue(messageResult.body(), RequestResult.class);
            if (requestId.equals(requestResult.getRequestId())) {
                context.assertFalse(requestResult.isSuccess(), requestResult.toString());
                context.assertEquals("dateTime is required", requestResult.getDescription());
                asyncReturnRequest.complete();
            }
        });

        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
        final String messageValue = "{\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Invalid Schema Test\"}";

        final Promise<Void> promise = Promise.promise();
        final ConsumerRecord<String, String> record = new ConsumerRecord<>("invalid.schema.topic", 0, 0, "2", messageValue);
        record.headers().add(ScheduleSchema.REQUEST_ID_HEADER, requestId.getBytes());
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
        this.vertx.eventBus().<String>consumer(INVALID_SCHEDULE_RECEIVED_ADDRESS, messageResult -> {
            final InvalidMessage invalidMessage = Json.decodeValue(messageResult.body(), InvalidMessage.class);
            context.assertEquals(InvalidMessageReason.VALUE_DECODE_FAILURE, invalidMessage.getReason());
            context.assertNotNull(invalidMessage.getCause());
            asyncErrorNotification.complete();
        });

        final String requestId = UUID.randomUUID().toString();
        final Async asyncReturnRequest = context.async();
        this.vertx.eventBus().<String>consumer(SCHEDULE_RETURN_REQUEST_PROCESSED_ADDRESS, messageResult -> {
            final RequestResult requestResult = Json.decodeValue(messageResult.body(), RequestResult.class);
            if (requestId.equals(requestResult.getRequestId())) {
                context.assertFalse(requestResult.isSuccess(), requestResult.toString());
                context.assertNotNull(requestResult.getDescription());
            }

            asyncReturnRequest.complete();
        });

        final ScheduleMessageProcessor scheduleMessageProcessor = ScheduleMessageProcessor.create(this.vertx);
        final String messageValue = "{\"dateTime\":\"12-04-2020\",\"customer\":{\"name\":\"Customer 1\",\"documentNumber\":948948393849,\"phone\":\"4499099493\"},\"description\":\"Invalid Json Test\"}";

        final Promise<Void> promise = Promise.promise();
        final ConsumerRecord<String, String> record = new ConsumerRecord<>("invalid.json.topic", 0, 0, "3", messageValue);
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