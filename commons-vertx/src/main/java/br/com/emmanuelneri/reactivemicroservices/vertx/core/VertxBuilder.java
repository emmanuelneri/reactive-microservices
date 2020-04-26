package br.com.emmanuelneri.reactivemicroservices.vertx.core;

import io.vertx.core.Vertx;

public final class VertxBuilder {

    public static Vertx createAndConfigure() {
        final Vertx vertx = Vertx.vertx();
        JsonConfiguration.setUpDefault();
        return vertx;
    }

}
