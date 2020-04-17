package br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces;

import br.com.emmanuelneri.reactivemicroservices.cassandra.codec.LocalDateTimeCodec;
import br.com.emmanuelneri.reactivemicroservices.cassandra.config.CassandraConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.ScheduleCommandEvents;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.domain.Schedule;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import io.vertx.cassandra.CassandraClient;
import io.vertx.cassandra.ResultSet;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AllArgsConstructor;

import static br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.MessageError.CONNECTION_ERROR;
import static br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.MessageError.EXECUTION_ERROR;

@AllArgsConstructor
public class SchedulePersistenceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVerticle.class);
    private static final String REPLY_OK = "ok";

    private final CassandraConfiguration cassandraConfiguration;

    @Override
    public void start(final Promise<Void> startPromise) {
        try {
            final CassandraClient client = CassandraClient.createShared(vertx, cassandraConfiguration.getOptions());
            this.vertx.eventBus().consumer(ScheduleCommandEvents.SCHEDULE_RECEIVED.getName(), persist(client));
            startPromise.complete();
            LOGGER.info("SchedulePersistenceVerticle started");
        } catch (final Exception ex) {
            startPromise.fail(ex);
        }
    }

    private Handler<Message<JsonObject>> persist(final CassandraClient client) {
        return message -> {
            client.prepare("INSERT INTO schedule (data_time, description, document_number, customer, phone, email) VALUES (?,?,?,?,?,?)", prepareResultHandler -> {
                if (prepareResultHandler.failed()) {
                    LOGGER.error("prepareStatement error", prepareResultHandler.cause());
                    message.fail(CONNECTION_ERROR.getCode(), prepareResultHandler.cause().getMessage());
                    return;
                }

                try {
                    final Schedule schedule = message.body().mapTo(Schedule.class);

                    final PreparedStatement preparedStatement = prepareResultHandler.result();
                    preparedStatement.getCodecRegistry().register(LocalDateTimeCodec.instance);

                    final BoundStatement boundStatement = preparedStatement
                            .bind(schedule.getDateTime(), schedule.getDescription(), schedule.getDocumentNumber(),
                                    schedule.getCustomer(), schedule.getPhone(), schedule.getEmail());

                    client.execute(boundStatement, handlerExecute(message));
                } catch (final Exception ex) {
                    LOGGER.error("boundStatement error", ex);
                    message.fail(EXECUTION_ERROR.getCode(), ex.getMessage());
                }
            });
        };
    }

    private Handler<AsyncResult<ResultSet>> handlerExecute(final Message<JsonObject> message) {
        return executeResultHandler -> {
            if (executeResultHandler.failed()) {
                LOGGER.error("execute error", executeResultHandler.cause());
                message.fail(CONNECTION_ERROR.getCode(), executeResultHandler.cause().getMessage());
                return;
            }

            message.reply(REPLY_OK);
        };
    }
}
