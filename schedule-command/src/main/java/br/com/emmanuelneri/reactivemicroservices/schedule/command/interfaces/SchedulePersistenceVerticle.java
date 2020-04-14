package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.cassandra.codec.LocalDateTimeCodec;
import br.com.emmanuelneri.reactivemicroservices.cassandra.config.CassandraConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.domain.Schedule;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import io.vertx.cassandra.CassandraClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SchedulePersistenceVerticle extends AbstractVerticle {

    private final CassandraConfiguration cassandraConfiguration;

    @Override
    public void start(final Future<Void> startFuture) {
        final CassandraClient client = CassandraClient.createShared(vertx, cassandraConfiguration.getOptions());
        this.vertx.eventBus().consumer(ScheduleCommandEvents.SCHEDULE_RECEIVED.getName(), persist(client));
    }

    private Handler<Message<JsonObject>> persist(final CassandraClient client) {
        return message -> {
            final Schedule schedule = message.body().mapTo(Schedule.class);
            client.prepare("INSERT INTO schedule (data_time, description, document_number, customer, phone, email) VALUES (?,?,?,?,?,?)", prepareResultHandler -> {
                if (prepareResultHandler.failed()) {
                    message.fail(999, prepareResultHandler.cause().getMessage());
                    return;
                }

                final PreparedStatement preparedStatement = prepareResultHandler.result();
                preparedStatement.getCodecRegistry().register(LocalDateTimeCodec.instance);
                final BoundStatement boundStatement = preparedStatement
                        .bind(schedule.getDateTime(), schedule.getDescription(), schedule.getDocumentNumber(),
                                schedule.getCustomer(), schedule.getPhone(), schedule.getEmail());

                client.execute(boundStatement, executeResultHandler -> {
                    if (prepareResultHandler.failed()) {
                        message.fail(999, executeResultHandler.cause().getMessage());
                        return;
                    }

                    message.reply("ok");
                });
            });
        };
    }
}