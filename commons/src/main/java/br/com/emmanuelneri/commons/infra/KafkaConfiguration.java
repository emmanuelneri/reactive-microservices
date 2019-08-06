package br.com.emmanuelneri.commons.infra;

import io.vertx.core.json.JsonObject;

public final class KafkaConfiguration {

    private final String kafkaBootstrapServers;
    private final String kafkaKeySerializer;
    private final String kafkaValueSerializer;

    public KafkaConfiguration(final JsonObject configuration) {
        this.kafkaBootstrapServers = configuration.getString("bootstrap.servers");
        this.kafkaKeySerializer = configuration.getString("key.serializer");
        this.kafkaValueSerializer = configuration.getString("value.serializer");
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
