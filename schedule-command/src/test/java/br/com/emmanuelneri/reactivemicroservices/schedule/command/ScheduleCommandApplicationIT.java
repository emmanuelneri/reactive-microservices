package br.com.emmanuelneri.reactivemicroservices.schedule.command;

import br.com.emmanuelneri.reactivemicroservices.cassandra.codec.LocalDateTimeCodec;
import br.com.emmanuelneri.reactivemicroservices.cassandra.config.CassandraConfiguration;
import br.com.emmanuelneri.reactivemicroservices.cassandra.test.CassandraTestConstants;
import br.com.emmanuelneri.reactivemicroservices.mapper.JsonConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleConsumerVerticle;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleRequestResultProducer;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.test.CassandraInit;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.CustomerSchema;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleRequestResult;
import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import br.com.emmanuelneri.reactivemicroservices.test.KafkaTestConstants;
import br.com.emmanuelneri.reactivemicroservices.test.KafkaTestConsumer;
import br.com.emmanuelneri.reactivemicroservices.test.KafkaTestProducer;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.TypeCodec;
import io.vertx.cassandra.CassandraClient;
import io.vertx.cassandra.ResultSet;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.KafkaContainer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RunWith(VertxUnitRunner.class)
public class ScheduleCommandApplicationIT {

    @Rule
    public CassandraContainer cassandra = new CassandraContainer(CassandraTestConstants.CASSANDRA_DOCKER_VERSION);

    @Rule
    public KafkaContainer kafka = new KafkaContainer(KafkaTestConstants.KAFKA_DOCKER_VERSION);

    private JsonObject configuration;
    private Vertx vertx;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
        JsonConfiguration.setUpDefault();
        final JsonObject cassandraConfiguration = CassandraInit.create().start(cassandra);

        this.configuration = new JsonObject()
                .put("kafka.bootstrap.servers", kafka.getBootstrapServers())
                .put("kafka.key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                .put("kafka.value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                .put("kafka.key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                .put("kafka.value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                .put("kafka.offset.reset", "earliest")
                .put("kafka.enable.auto.commit", false);

        this.configuration.mergeIn(cassandraConfiguration);
    }

    @After
    public void after() {
        this.vertx.close();
    }

    @Test
    public void shouldProcessSchedule(final TestContext context) {
        final CustomerSchema customerSchema = new CustomerSchema();
        customerSchema.setDocumentNumber("948948393849");
        customerSchema.setName("Customer 1");
        customerSchema.setPhone("4499099493");

        final ScheduleSchema schema = new ScheduleSchema();
        schema.setCustomer(customerSchema);
        schema.setDateTime(LocalDateTime.now().plusDays(1));
        schema.setDescription("Complete Test");

        final String requestId = UUID.randomUUID().toString();
        KafkaTestProducer.create(vertx, configuration).send(ScheduleConsumerVerticle.SCHEDULE_REQUEST_TOPIC,
                customerSchema.getDocumentNumber(), Json.encode(schema), ScheduleSchema.REQUEST_ID_HEADER, requestId);

        ScheduleCommandApplication.start(vertx, configuration);

        final Async async = context.async();

        final KafkaConsumer<String, String> kafkaConsumer = KafkaTestConsumer.create(vertx, configuration)
                .build(ScheduleRequestResultProducer.SCHEDULE_REQUEST_TOPIC);

        kafkaConsumer.handler(consumerRecord -> {
            final ScheduleRequestResult scheduleRequestResult = Json.decodeValue(consumerRecord.value(), ScheduleRequestResult.class);
            context.assertTrue(scheduleRequestResult.isSuccess());
            context.assertEquals(requestId, scheduleRequestResult.getRequestId());

            final CassandraConfiguration cassandraConfiguration = new CassandraConfiguration(configuration);
            final CassandraClient client = CassandraClient.createShared(vertx, cassandraConfiguration.getOptions());
            client.execute("SELECT * FROM schedule", queryResultHandler -> {
                if (queryResultHandler.failed()) {
                    context.fail(queryResultHandler.cause());
                    return;
                }

                final ResultSet resultSet = queryResultHandler.result();
                resultSet.all(resultSetHandler -> {
                    if (resultSetHandler.failed()) {
                        context.fail(resultSetHandler.cause());
                        return;
                    }

                    final List<Row> rows = resultSetHandler.result();
                    context.assertEquals(1, rows.size());

                    final Row row = rows.get(0);
                    context.assertEquals(schema.getDescription(), row.get("description", TypeCodec.varchar()));
                    context.assertEquals(schema.getDateTime(), row.get("data_time", LocalDateTimeCodec.instance));
                    context.assertEquals(customerSchema.getName(), row.get("customer", TypeCodec.varchar()));
                    context.assertEquals(customerSchema.getDocumentNumber(), row.get("document_number", TypeCodec.varchar()));
                    context.assertEquals(customerSchema.getPhone(), row.get("phone", TypeCodec.varchar()));
                    context.assertEquals(customerSchema.getEmail(), row.get("email", TypeCodec.varchar()));
                    async.complete();
                });
            });
        });
    }
}