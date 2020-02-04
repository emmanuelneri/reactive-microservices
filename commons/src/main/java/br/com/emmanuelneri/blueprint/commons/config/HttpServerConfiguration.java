package br.com.emmanuelneri.blueprint.commons.config;

import io.vertx.core.json.JsonObject;

public final class HttpServerConfiguration {

    private static final String SERVER_PORT_PROPERTY = "server.port";
    private final int port;

    public HttpServerConfiguration(final JsonObject configuration) {
        this.port = configuration.getInteger(SERVER_PORT_PROPERTY);
    }

    public int getPort() {
        return port;
    }
}
