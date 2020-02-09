package br.com.emmanuelneri.blueprint.schedule.connector.interfaces;

import br.com.emmanuelneri.blueprint.mapper.JsonConfiguration;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.Objects;

@RunWith(VertxUnitRunner.class)
public class ScheduleEndpointIT {

    private static final int PORT = 9999;
    private static final String HOST = "localhost";
    private static final String URI = "/schedules";

    private Vertx vertx;

    @Before
    public void before() {
        if (Objects.nonNull(this.vertx)) {
            return;
        }

        this.vertx = Vertx.vertx();
        JsonConfiguration.setUpDefault();

        Router router = Router.router(vertx);
        vertx.deployVerticle(new ScheduleProcessor());
        vertx.deployVerticle(new ScheduleEndpoint((router)));

        final HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router)
                .listen(PORT);
    }

    @Test
    public void shouldProcessSchema(final TestContext context) {
        final Async async = context.async();
        final CustomerScheduleSchema customerSchema = new CustomerScheduleSchema();
        customerSchema.setDocumentNumber("043030493");
        customerSchema.setName("Customer");
        customerSchema.setPhone("4499099493");

        final ScheduleEndpointSchema schema = new ScheduleEndpointSchema();
        schema.setCustomer(customerSchema);
        schema.setDateTime(LocalDateTime.now().plusDays(1));
        schema.setDescription("Test");

        final WebClient client = WebClient.create(vertx);
        client.post(PORT, HOST, URI)
                .sendJson(schema, clientAsyncResult -> {
                    Assert.assertFalse(clientAsyncResult.failed());
                    final HttpResponse<Buffer> result = clientAsyncResult.result();
                    Assert.assertEquals(201, result.statusCode());
                    async.complete();
                });
    }

    @Test
    public void shouldReturnBandRequestWithInvalidSchema(final TestContext context) {
        final Async async = context.async();
        final WebClient client = WebClient.create(vertx);

        final String schema = "{\"desc\":\123}";
        client.post(PORT, HOST, URI)
                .sendJson(schema, clientAsyncResult -> {
                    final HttpResponse<Buffer> result = clientAsyncResult.result();
                    Assert.assertEquals(400, result.statusCode());
                    Assert.assertEquals("Invalid schema: Failed to decode:Cannot construct instance of `br.com.emmanuelneri.schedule.schema.ScheduleEndpointSchema` (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value ('{\"desc\":S}')\n" +
                            " at [Source: (String)\"\"{\\\"desc\\\":S}\"\"; line: 1, column: 1]", result.bodyAsString());
                    async.complete();
                });
    }

    @Test
    public void shouldReturnBandRequestWithNoValidSchedule(final TestContext context) {
        final Async async = context.async();
        final WebClient client = WebClient.create(vertx);

        final CustomerScheduleSchema customerSchema = new CustomerScheduleSchema();
        customerSchema.setDocumentNumber("043030493");
        customerSchema.setName("Customer");
        customerSchema.setPhone("4499099493");

        final ScheduleEndpointSchema schema = new ScheduleEndpointSchema();
        schema.setCustomer(customerSchema);
        schema.setDateTime(LocalDateTime.now().minusHours(1));
        schema.setDescription("Test");

        client.post(PORT, HOST, URI)
                .sendJson(schema, clientAsyncResult -> {
                    final HttpResponse<Buffer> result = clientAsyncResult.result();
                    Assert.assertEquals(400, result.statusCode());
                    Assert.assertEquals("dateTime invalid. Past dateTime is not allowed", result.bodyAsString());
                    async.complete();
                });
    }

}