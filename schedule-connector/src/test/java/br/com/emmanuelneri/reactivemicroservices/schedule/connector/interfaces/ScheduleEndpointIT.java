package br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces;

import br.com.emmanuelneri.reactivemicroservices.mapper.JsonConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.ScheduleEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.schema.Customer;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.schema.Schedule;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.usecase.ScheduleProcessor;
import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.ReplyResult;
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

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
        JsonConfiguration.setUpDefault();
        mockProducerRequest();
    }

    @After
    public void after() {
        this.vertx.close();
    }

    @Test
    public void shouldProcessSchema(final TestContext context) {
        final Customer customerSchema = new Customer();
        customerSchema.setDocumentNumber("043030493");
        customerSchema.setName("Customer");
        customerSchema.setPhone("4499099493");

        final Schedule schema = new Schedule();
        schema.setCustomer(customerSchema);
        schema.setDateTime(LocalDateTime.now().plusDays(1));
        schema.setDescription("Test");

        final Router router = Router.router(this.vertx);
        this.vertx.deployVerticle(new ScheduleProcessor());
        this.vertx.deployVerticle(new ScheduleEndpoint((router)));

        final WebClient client = WebClient.create(this.vertx);
        final HttpServer httpServer = this.vertx.createHttpServer();

        final Async async = context.async();
        httpServer.requestHandler(router)
                .listen(PORT, serverAsyncResult -> {
                    if (serverAsyncResult.failed()) {
                        context.fail(serverAsyncResult.cause());
                    }

                    client.post(PORT, HOST, URI)
                            .sendJson(schema, clientAsyncResult -> {
                                if (clientAsyncResult.failed()) {
                                    context.fail(clientAsyncResult.cause());
                                }

                                final HttpResponse<Buffer> result = clientAsyncResult.result();
                                context.assertEquals(201, result.statusCode());

                                httpServer.close();
                                async.complete();
                            });
                });
    }

    @Test
    public void shouldReturnBandRequestWithInvalidSchema(final TestContext context) {
        final String schema = "{\"desc\":\123}";

        final Router router = Router.router(this.vertx);
        this.vertx.deployVerticle(new ScheduleProcessor());
        this.vertx.deployVerticle(new ScheduleEndpoint((router)));

        final WebClient client = WebClient.create(this.vertx);
        final HttpServer httpServer = this.vertx.createHttpServer();

        final Async async = context.async();
        httpServer.requestHandler(router)
                .listen(PORT, serverAsyncResult -> {
                    if (serverAsyncResult.failed()) {
                        context.fail(serverAsyncResult.cause());
                    }

                    client.post(PORT, HOST, URI)
                            .sendJson(schema, clientAsyncResult -> {
                                if (clientAsyncResult.failed()) {
                                    context.fail(clientAsyncResult.cause());
                                }

                                final HttpResponse<Buffer> result = clientAsyncResult.result();
                                context.assertEquals(400, result.statusCode());
                                context.assertEquals("invalid schema: \"{\\\"desc\\\":S}\"", result.bodyAsString());

                                httpServer.close();
                                async.complete();
                            });
                });
    }

    @Test
    public void shouldReturnBandRequestWithNoValidSchedule(final TestContext context) {
        final Customer customerSchema = new Customer();
        customerSchema.setDocumentNumber("043030493");
        customerSchema.setName("Customer");
        customerSchema.setPhone("4499099493");

        final Schedule schema = new Schedule();
        schema.setCustomer(customerSchema);
        schema.setDateTime(LocalDateTime.now().minusHours(1));
        schema.setDescription("Test");

        final Router router = Router.router(this.vertx);
        this.vertx.deployVerticle(new ScheduleProcessor());
        this.vertx.deployVerticle(new ScheduleEndpoint((router)));

        final WebClient client = WebClient.create(this.vertx);
        final HttpServer httpServer = this.vertx.createHttpServer();

        final Async async = context.async();
        httpServer.requestHandler(router)
                .listen(PORT, serverAsyncResult -> {
                    if (serverAsyncResult.failed()) {
                        context.fail(serverAsyncResult.cause());
                    }

                    client.post(PORT, HOST, URI)
                            .sendJson(schema, clientAsyncResult -> {
                                if (clientAsyncResult.failed()) {
                                    context.fail(clientAsyncResult.cause());
                                }

                                final HttpResponse<Buffer> result = clientAsyncResult.result();
                                context.assertEquals(400, result.statusCode());
                                context.assertEquals("dateTime invalid. Past dateTime is not allowed", result.bodyAsString());

                                httpServer.close();
                                async.complete();
                            });
                });
    }

    private void mockProducerRequest() {
        this.vertx.eventBus().localConsumer(ScheduleEvents.SCHEDULE_VALIDATED.name(),
                message -> message.reply(ReplyResult.OK.asJson()));
    }
}