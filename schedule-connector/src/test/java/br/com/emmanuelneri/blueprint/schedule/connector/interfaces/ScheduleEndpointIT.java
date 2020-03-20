package br.com.emmanuelneri.blueprint.schedule.connector.interfaces;

import br.com.emmanuelneri.blueprint.mapper.JsonConfiguration;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.Events;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.ProcessorResult;
import br.com.emmanuelneri.blueprint.schedule.connector.service.ScheduleProcessor;
import br.com.emmanuelneri.schedule.schema.CustomerScheduleSchema;
import br.com.emmanuelneri.schedule.schema.ScheduleEndpointSchema;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;

@RunWith(VertxUnitRunner.class)
public class ScheduleEndpointIT {

    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private static final String URI = "/schedules";

    private Vertx vertx;
    private HttpServer httpServer;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
        JsonConfiguration.setUpDefault();

        final Router router = Router.router(this.vertx);
        this.vertx.deployVerticle(new ScheduleProcessor());
        this.vertx.deployVerticle(new ScheduleEndpoint((router)));

        mockProducerRequest();

        this.httpServer = this.vertx.createHttpServer();
        this.httpServer.requestHandler(router)
                .listen(PORT);
    }

    @After
    public void after() {
        this.httpServer.close();
        this.vertx.close();
    }

    @Test
    public void shouldProcessSchema(final TestContext context) {
        final CustomerScheduleSchema customerSchema = new CustomerScheduleSchema();
        customerSchema.setDocumentNumber("043030493");
        customerSchema.setName("Customer");
        customerSchema.setPhone("4499099493");

        final ScheduleEndpointSchema schema = new ScheduleEndpointSchema();
        schema.setCustomer(customerSchema);
        schema.setDateTime(LocalDateTime.now().plusDays(1));
        schema.setDescription("Test");

        final WebClient client = WebClient.create(this.vertx);
        final Async async = context.async();
        client.post(PORT, HOST, URI)
                .sendJson(schema, clientAsyncResult -> {
                    context.assertFalse(clientAsyncResult.failed());
                    final HttpResponse<Buffer> result = clientAsyncResult.result();
                    context.assertEquals(201, result.statusCode());
                    async.complete();
                });
    }

    @Test
    public void shouldReturnBandRequestWithInvalidSchema(final TestContext context) {
        final String schema = "{\"desc\":\123}";

        final WebClient client = WebClient.create(this.vertx);
        final Async async = context.async();
        client.post(PORT, HOST, URI)
                .sendJson(schema, clientAsyncResult -> {
                    final HttpResponse<Buffer> result = clientAsyncResult.result();
                    context.assertEquals(400, result.statusCode());
                    context.assertEquals("Invalid schema: Failed to decode:Cannot construct instance of `br.com.emmanuelneri.schedule.schema.ScheduleEndpointSchema` (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value ('{\"desc\":S}')\n" +
                            " at [Source: (String)\"\"{\\\"desc\\\":S}\"\"; line: 1, column: 1]", result.bodyAsString());
                    async.complete();
                });
    }

    @Test
    public void shouldReturnBandRequestWithNoValidSchedule(final TestContext context) {
        final CustomerScheduleSchema customerSchema = new CustomerScheduleSchema();
        customerSchema.setDocumentNumber("043030493");
        customerSchema.setName("Customer");
        customerSchema.setPhone("4499099493");

        final ScheduleEndpointSchema schema = new ScheduleEndpointSchema();
        schema.setCustomer(customerSchema);
        schema.setDateTime(LocalDateTime.now().minusHours(1));
        schema.setDescription("Test");

        final WebClient client = WebClient.create(this.vertx);
        final Async async = context.async();
        client.post(PORT, HOST, URI)
                .sendJson(schema, clientAsyncResult -> {
                    final HttpResponse<Buffer> result = clientAsyncResult.result();
                    context.assertEquals(400, result.statusCode());
                    context.assertEquals("dateTime invalid. Past dateTime is not allowed", result.bodyAsString());
                    async.complete();
                });
    }

    private void mockProducerRequest() {
        this.vertx.eventBus().localConsumer(Events.SCHEDULE_VALIDATED.name(),
                message -> message.reply(ProcessorResult.OK_AS_JSON));
    }

}