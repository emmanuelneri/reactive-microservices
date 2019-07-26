package br.com.emmanuelneri.orders.web;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import static br.com.emmanuelneri.orders.infra.EventBusAddress.RECEIVED_ORDER;

public class OrderRoutingHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(OrderRoutingHandler.class);

    private final Vertx vertx;

    public OrderRoutingHandler(final Vertx vertx) {
        this.vertx = vertx;
    }

    public Handler<RoutingContext> addOrder() {
        return routingContext -> {
            final JsonObject order = getOrderAsJson(routingContext);
            LOGGER.info("order received {0}", order);

            sendOrderToEventBus(order);
            routingContext.response().end();
        };
    }

    private void sendOrderToEventBus(final JsonObject order) {
        vertx.eventBus().send(RECEIVED_ORDER.getAddress(), Json.encode(order));
    }

    private JsonObject getOrderAsJson(final RoutingContext routingContext) {
        return routingContext.getBodyAsJson();
    }
}
