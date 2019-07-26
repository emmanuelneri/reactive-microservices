package br.com.emmanuelneri.orders.infra;

import io.vertx.core.json.JsonObject;

public final class Configuration {

    private final int httpServerPort;
    private final String kafkaBootstrapServers;
    private final String kafkaKeySerializer;
    private final String kafkaValueSerializer;

    public Configuration(final JsonObject configuration) {
        this.httpServerPort = configuration.getInteger("server.port");
        this.kafkaBootstrapServers = configuration.getString("bootstrap.servers");
        this.kafkaKeySerializer = configuration.getString("key.serializer");
        this.kafkaValueSerializer = configuration.getString("value.serializer");
    }

    public int getHttpServerPort() {
        return httpServerPort;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public String getKafkaKeySerializer() {
        return kafkaKeySerializer;
    }

    public String getKafkaValueSerializer() {
        return kafkaValueSerializer;
    }
}
