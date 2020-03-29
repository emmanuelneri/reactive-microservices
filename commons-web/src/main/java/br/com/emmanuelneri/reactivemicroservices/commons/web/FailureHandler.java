package br.com.emmanuelneri.reactivemicroservices.commons.web;

import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public final class FailureHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailureHandler.class);

    @Override
    public void handle(final RoutingContext routingContext) {
        LOGGER.error("Router error", routingContext.failure());
        routingContext.response()
                .setStatusCode(routingContext.statusCode())
                .setStatusMessage(routingContext.getBodyAsString())
                .end();
    }
}
