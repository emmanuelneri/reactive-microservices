package br.com.emmanuelneri.commons.infra;

import io.vertx.core.json.JsonObject;

public final class HttpServerConfiguration {

    private final int port;

    public HttpServerConfiguration(final JsonObject configuration) {
        this.port = configuration.getInteger("server.port");
    }

    public int getPort() {
        return port;
    }
}
