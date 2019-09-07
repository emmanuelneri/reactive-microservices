package br.com.emmanuelneri.orders.verticle;

import br.com.emmanuelneri.order.schema.OrderSchema;
import br.com.emmanuelneri.orders.domain.Order;
import br.com.emmanuelneri.orders.infra.EventBusAddress;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Tuple;

public class OrderStorageVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStorageVerticle.class);

    private final PgPool pgPool;

    public OrderStorageVerticle(final PgPool pgPool) {
        this.pgPool = pgPool;
    }

    @Override
    public void start(final Future<Void> startFuture) {
        final EventBus eventBus = this.vertx.eventBus();

        eventBus.localConsumer(EventBusAddress.RECEIVED_ORDER.getAddress(), message -> {

            toDomain(message.body().toString(), order -> {
                final Tuple tuple = Tuple.of(order.getIdentifier(), order.getValue());
                pgPool.preparedQuery("INSERT INTO SALES_ORDER (identifier, value) VALUES ($1, $2)", tuple, handler -> {
                    if (handler.failed()) {
                        LOGGER.error("query error", handler.cause());
                        return;
                    }
                    LOGGER.info("order saved. {0}", handler.result());
                });
            }, error -> LOGGER.error("schema to domain error", error));
        });
    }

    private void toDomain(final String messageBody, final Handler<Order> orderHandler, final Handler<Throwable> errorHandler) {
        try {
            final OrderSchema schema = Json.decodeValue(messageBody, OrderSchema.class);
            final Order order = new Order(schema.getIdentifier(), schema.getValue());
            orderHandler.handle(order);
        } catch (Exception ex) {
            errorHandler.handle(ex);
        }
    }
}
