package br.com.emmanuelneri.blueprint.commons.config;

import io.vertx.core.json.JsonObject;

import java.util.Objects;

public final class HttpServerConfiguration {

    static final String SERVER_PORT_PROPERTY = "server.port";
    static final int DEFAULT_PORT = 8080;
    private final int port;

    public HttpServerConfiguration(final JsonObject configuration) {
        final Integer configurationPort = configuration.getInteger(SERVER_PORT_PROPERTY);
        this.port = Objects.nonNull(configurationPort) ? configurationPort : DEFAULT_PORT;
    }

    public int getPort() {
        return port;
    }
}
