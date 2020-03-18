package br.com.emmanuelneri.blueprint.schedule.connector.interfaces;

import br.com.emmanuelneri.blueprint.kafka.KafkaConsumerConfiguration;
import br.com.emmanuelneri.blueprint.kafka.KafkaProducerConfiguration;
import br.com.emmanuelneri.blueprint.mapper.JsonConfiguration;
import br.com.emmanuelneri.blueprint.schedule.connector.service.ScheduleProcessor;
import br.com.emmanuelneri.schedule.schema.CustomerScheduleSchema;
import br.com.emmanuelneri.schedule.schema.ScheduleEndpointSchema;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.KafkaContainer;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@RunWith(VertxUnitRunner.class)
public class ScheduleConnectorIT {

    private static final int PORT = 8888;
    private static final String HOST = "localhost";
    private static final String URI = "/schedules";

    private Vertx vertx;
    private HttpServer httpServer;

    @Rule
    public KafkaContainer kafka = new KafkaContainer("5.2.1");
    private KafkaConsumer<String, String> kafkaConsumer;

    @Before
    public void before() {
        final JsonObject configuration = new JsonObject()
                .put("kafka.bootstrap.servers", kafka.getBootstrapServers())
                .put("kafka.key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                .put("kafka.value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                .put("kafka.key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                .put("kafka.value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                .put("kafka.offset.reset", "earliest");

        this.vertx = Vertx.vertx();

        JsonConfiguration.setUpDefault();
        final KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);
        final Router router = Router.router(vertx);

        this.vertx.deployVerticle(new ScheduleProcessor());
        this.vertx.deployVerticle(new ScheduleProducer(kafkaProducerConfiguration));
        this.vertx.deployVerticle(new ScheduleEndpoint((router)));

        final Map<String, String> kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration).createConfig("test-schedule-consumer");
        this.kafkaConsumer = KafkaConsumer.create(this.vertx, kafkaConsumerConfiguration);
        this.kafkaConsumer.subscribe(ScheduleProducer.SCHEDULE_REQUEST_TOPIC);

        this.httpServer = this.vertx.createHttpServer();
        httpServer.requestHandler(router)
                .listen(PORT);
    }

    @After
    public void after() {
        this.httpServer.close();
        this.vertx.close();
    }

    @Test
    public void shouldProcessSchedule(final TestContext context) {
        final CustomerScheduleSchema customerSchema = new CustomerScheduleSchema();
        customerSchema.setDocumentNumber("948948393849");
        customerSchema.setName("Customer 1");
        customerSchema.setPhone("4499099493");

        final ScheduleEndpointSchema schedule = new ScheduleEndpointSchema();
        schedule.setCustomer(customerSchema);
        schedule.setDateTime(LocalDateTime.now().plusDays(1));
        schedule.setDescription("Complete Test");

        final WebClient client = WebClient.create(vertx);
        final Async async = context.async();
        client.post(PORT, HOST, URI)
                .sendJson(schedule, clientAsyncResult -> {
                    Assert.assertFalse(clientAsyncResult.failed());
                    final HttpResponse<Buffer> result = clientAsyncResult.result();
                    Assert.assertEquals(201, result.statusCode());

                    kafkaConsumer.handler(consumerRecord -> {
                        Assert.assertNotNull(consumerRecord.key());
                        Assert.assertEquals(Json.encode(schedule), consumerRecord.value());
                        async.complete();
                    });
                });
    }
}