package br.com.emmanuelneri.orders.web;

import br.com.emmanuelneri.order.schema.OrderSchema;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import static br.com.emmanuelneri.orders.infra.EventBusAddress.RECEIVED_ORDER;

public class OrderRoutingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderRoutingHandler.class);

    private final Vertx vertx;

    public OrderRoutingHandler(final Vertx vertx) {
        this.vertx = vertx;
    }

    public Handler<RoutingContext> orderProcessContext() {
        return routingContext -> {
            LOGGER.info("order received {0}", routingContext.getBodyAsString());
            convert(routingContext, orderSchema -> {
                process(orderSchema);
                routingContext.response().end();
            }, error -> {
                LOGGER.error("invalid order", error);
                routingContext.response()
                        .setStatusCode(400)
                        .setStatusMessage("invalid order")
                        .end();
            });
        };
    }

    private void convert(final RoutingContext routingContext, final Handler<OrderSchema> successHandler, final Handler<Throwable> errorHandler) {
        try {
            final String body = routingContext.getBodyAsString();
            successHandler.handle(Json.mapper.readValue(body, OrderSchema.class));
        } catch (Exception ex) {
            errorHandler.handle(ex);
        }
    }

    private void process(final OrderSchema orderSchema) {
        vertx.eventBus().publish(RECEIVED_ORDER.getAddress(), Json.encode(orderSchema));
    }
}
