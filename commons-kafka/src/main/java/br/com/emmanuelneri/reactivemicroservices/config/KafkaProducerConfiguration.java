package br.com.emmanuelneri.reactivemicroservices.config;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public final class KafkaProducerConfiguration extends KafkaConfiguration {

    private static final String DEFAULT_ACKS = "1";

    protected final String keySerializer;
    protected final String valueSerializer;

    public KafkaProducerConfiguration(final JsonObject configuration) {
        super(configuration);
        this.keySerializer = configuration.getString("kafka.key.serializer");
        this.valueSerializer = configuration.getString("kafka.value.serializer");
    }

    public Map<String, String> createConfig() {
        final Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", this.bootstrapServers);
        config.put("key.serializer", this.keySerializer);
        config.put("value.serializer", this.valueSerializer);
        config.put("acks", DEFAULT_ACKS);
        return config;
    }
}
