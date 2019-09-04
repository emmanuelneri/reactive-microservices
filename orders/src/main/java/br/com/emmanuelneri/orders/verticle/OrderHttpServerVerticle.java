package br.com.emmanuelneri.orders.verticle;

import br.com.emmanuelneri.commons.infra.HttpServerConfiguration;
import br.com.emmanuelneri.orders.web.FailureHandler;
import br.com.emmanuelneri.orders.web.OrderRoutingHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class OrderHttpServerVerticle extends AbstractVerticle {

    private final HttpServerConfiguration configuration;

    public OrderHttpServerVerticle(final HttpServerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start(final Future<Void> startFuture) {
        final HttpServer httpServer = vertx.createHttpServer();

        final FailureHandler failureHandler = new FailureHandler();
        final OrderRoutingHandler orderRoutingHandler = new OrderRoutingHandler(vertx);

        final Router router =  Router.router(vertx);
        router.route("/orders/*").handler(BodyHandler.create()).failureHandler(failureHandler);
        router.post("/orders").handler(orderRoutingHandler.orderProcessContext()).failureHandler(failureHandler);

        httpServer.requestHandler(router)
                .listen(configuration.getPort());
    }

}
