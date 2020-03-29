package br.com.emmanuelneri.reactivemicroservices.config;

import io.vertx.core.json.JsonObject;

public class KafkaConfiguration {

    protected final String bootstrapServers;

    public KafkaConfiguration(final JsonObject configuration) {
        this.bootstrapServers = configuration.getString("kafka.bootstrap.servers");
    }
}
