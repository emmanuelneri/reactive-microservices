package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.cassandra.codec.LocalDateTimeCodec;
import br.com.emmanuelneri.reactivemicroservices.cassandra.config.CassandraConfiguration;
import br.com.emmanuelneri.reactivemicroservices.mapper.JsonConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.domain.Schedule;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TypeCodec;
import io.vertx.cassandra.CassandraClient;
import io.vertx.cassandra.ResultSet;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.CassandraContainer;

import java.time.LocalDateTime;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class SchedulePersistenceVerticleIT {

    @Rule
    public CassandraContainer cassandra = new CassandraContainer("cassandra:3.11");

    private JsonObject configuration;
    private Vertx vertx;

    @Before
    public void before() {
        final Cluster cluster = cassandra.getCluster();

        this.vertx = Vertx.vertx();
        JsonConfiguration.setUpDefault();

        try (final Session session = cluster.connect()) {
            final String address = session.getCluster().getMetadata().getAllHosts().stream().findAny().get().toString();
            final String contactPoint = address.substring(address.indexOf("/") + 1, address.indexOf(":"));
            final String port = address.substring(address.lastIndexOf(":") + 1);

            configuration = new JsonObject()
                    .put("cassandra.contactPoint", contactPoint)
                    .put("cassandra.port", Integer.valueOf(port))
                    .put("cassandra.keyspace", "test");


            session.execute("CREATE KEYSPACE IF NOT EXISTS test WITH replication = " +
                    "{'class':'SimpleStrategy','replication_factor':'1'};");

            session.execute("USE test");

            session.execute("CREATE TABLE IF NOT EXISTS test.schedule " +
                    "(data_time timestamp, description text, document_number text, customer text, phone text, email text, " +
                    "PRIMARY KEY(data_time, description, document_number))");
        }
    }

    @After
    public void after() {
        this.vertx.close();
    }

    @Test
    public void shouldPersistSchedule(final TestContext context) {
        final CassandraConfiguration cassandraConfiguration = new CassandraConfiguration(this.configuration);
        this.vertx.deployVerticle(new SchedulePersistenceVerticle(cassandraConfiguration));

        final Schedule schedule = new Schedule();
        schedule.setDateTime(LocalDateTime.now().plusDays(1));
        schedule.setDescription("Complete Test");
        schedule.setDocumentNumber("948948393849");
        schedule.setCustomer("Customer 1");
        schedule.setPhone("4499099493");
        schedule.setEmail("test@gmail.com");

        final Async async = context.async();
        this.vertx.eventBus().request(ScheduleCommandEvents.SCHEDULE_RECEIVED.getName(), JsonObject.mapFrom(schedule), requestResultHandler -> {
            if (requestResultHandler.failed()) {
                context.fail(requestResultHandler.cause());
                return;
            }

            final Object body = requestResultHandler.result().body();
            context.assertNotNull(body);
            context.assertEquals("ok", body.toString());

            final CassandraClient client = CassandraClient.create(vertx, cassandraConfiguration.getOptions());
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
                    context.assertEquals(schedule.getDescription(), row.get("description", TypeCodec.varchar()));
                    context.assertEquals(schedule.getDateTime(), row.get("data_time", LocalDateTimeCodec.instance));
                    context.assertEquals(schedule.getCustomer(), row.get("customer", TypeCodec.varchar()));
                    context.assertEquals(schedule.getDocumentNumber(), row.get("document_number", TypeCodec.varchar()));
                    context.assertEquals(schedule.getPhone(), row.get("phone", TypeCodec.varchar()));
                    context.assertEquals(schedule.getEmail(), row.get("email", TypeCodec.varchar()));
                    async.complete();
                });
            });
        });
    }

    @Test
    @Ignore
    public void shouldReplyFailWithDBIsOut(final TestContext context) {
        cassandra.stop();

        final CassandraConfiguration cassandraConfiguration = new CassandraConfiguration(this.configuration);
        this.vertx.deployVerticle(new SchedulePersistenceVerticle(cassandraConfiguration));

        final Schedule schedule = new Schedule();

        final Async async = context.async();
        this.vertx.eventBus().request(ScheduleCommandEvents.SCHEDULE_RECEIVED.getName(), JsonObject.mapFrom(schedule), requestResultHandler -> {
            context.assertTrue(requestResultHandler.failed());
            context.assertNotNull(requestResultHandler.cause());
            async.complete();
        });
    }
}