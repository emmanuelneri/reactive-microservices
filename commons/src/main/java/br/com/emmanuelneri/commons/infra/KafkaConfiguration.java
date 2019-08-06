package br.com.emmanuelneri.commons.infra;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public final class KafkaConfiguration {

    private final String bootstrapServers;
    private final String keySerializer;
    private final String valueSerializer;
    private final String keyDeserializer;
    private final String valueDeserializer;

    public KafkaConfiguration(final JsonObject configuration) {
        this.bootstrapServers = configuration.getString("bootstrap.servers");
        this.keySerializer = configuration.getString("key.serializer");
        this.valueSerializer = configuration.getString("value.serializer");
        this.keyDeserializer = configuration.getString("value.deserializer");
        this.valueDeserializer = configuration.getString("value.deserializer");
    }

    public Map<String, String> createKafkaProducerConfig() {
        final Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", this.bootstrapServers);
        config.put("key.serializer", this.keySerializer);
        config.put("value.serializer", this.valueSerializer);
        return config;
    }

    public Map<String, String> createKafkaConsumerConfig(final String consumerGroupId) {
        final Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", this.bootstrapServers);
        config.put("key.deserializer", this.keyDeserializer);
        config.put("value.deserializer", this.valueDeserializer);
        config.put("group.id", consumerGroupId);
        config.put("auto.offset.reset", "earliest");
        return config;
    }
}
