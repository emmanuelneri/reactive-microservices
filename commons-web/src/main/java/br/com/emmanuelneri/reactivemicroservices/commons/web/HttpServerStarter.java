package br.com.emmanuelneri.reactivemicroservices.commons.web;

import br.com.emmanuelneri.reactivemicroservices.commons.config.HttpServerConfiguration;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpServerStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerStarter.class);

    private final Vertx vertx;
    private final HttpServerConfiguration configuration;

    public static HttpServerStarter create(final Vertx vertx, final JsonObject configuration) {
        return new HttpServerStarter(vertx, new HttpServerConfiguration(configuration));
    }

    public void start(final Router router, final Handler<AsyncResult<HttpServer>> startHandler) {
        final HttpServer httpServer = this.vertx.createHttpServer();
        httpServer.requestHandler(router)
                .listen(getPort(), startHandler);
    }

    public void start(final Router router) {
        start(router, asyncResult -> {
            if (asyncResult.failed()) {
                LOGGER.error("failed to start http server");
                return;
            }

            LOGGER.info("http server started on port {0}", getPort());
        });
    }

    private int getPort() {
        return configuration.getPort();
    }
}
