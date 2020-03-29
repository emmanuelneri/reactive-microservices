package br.com.emmanuelneri.reactivemicroservices.commons.web;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class HttpServerStarterTest {

    @Test
    public void start(final TestContext context) {
        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);

        final JsonObject configuration = new JsonObject()
                .put("server.port", 8099);

        HttpServerStarter.create(vertx, configuration)
                .start(router, asyncResult -> {
                    if (asyncResult.failed()) {
                        context.fail(asyncResult.cause());
                    }

                    vertx.close();
                    context.asyncAssertSuccess();
                });

    }
}