package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.cassandra.codec.LocalDateTimeCodec;
import br.com.emmanuelneri.reactivemicroservices.cassandra.config.CassandraConfiguration;
import br.com.emmanuelneri.reactivemicroservices.cassandra.test.CassandraTestConstants;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.domain.Schedule;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.test.CassandraInit;
import br.com.emmanuelneri.reactivemicroservices.vertx.core.VertxBuilder;
import com.datastax.driver.core.Row;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.CassandraContainer;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.SchedulePersistenceVerticle.SCHEDULE_RECEIVED_ADDRESS;

@RunWith(VertxUnitRunner.class)
public class SchedulePersistenceVerticleIT {

    @Rule
    public CassandraContainer cassandra = new CassandraContainer(CassandraTestConstants.CASSANDRA_DOCKER_VERSION);

    private JsonObject configuration;
    private Vertx vertx;

    @Before
    public void before() {
        this.vertx = VertxBuilder.createAndConfigure();
        this.configuration = CassandraInit.create().start(cassandra);
    }

    @After
    public void after() {
        this.vertx.close();
    }

    @Test // TODO melhorar
    public void shouldPersistSchedule(final TestContext context) {
        final CassandraConfiguration cassandraConfiguration = new CassandraConfiguration(this.configuration);

        final Schedule schedule = new Schedule();
        schedule.setDateTime(LocalDateTime.now().plusDays(1));
        schedule.setDescription("Complete Test");
        schedule.setDocumentNumber("948948393849");
        schedule.setCustomer("Customer 1");
        schedule.setPhone("4499099493");
        schedule.setEmail("test@gmail.com");


        final Async async = context.async();
        this.vertx.deployVerticle(new SchedulePersistenceVerticle(cassandraConfiguration), deployHandler -> {
            if (deployHandler.failed()) {
                context.fail(deployHandler.cause());
            }

            this.vertx.eventBus().request(SCHEDULE_RECEIVED_ADDRESS, JsonObject.mapFrom(schedule), requestResultHandler -> {
                if (requestResultHandler.failed()) {
                    context.fail(requestResultHandler.cause());
                    return;
                }

                final Object body = requestResultHandler.result().body();
                context.assertNotNull(body);
                context.assertEquals("ok", body.toString());

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
        });
    }

    @Test
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