package br.com.emmanuelneri.reactivemicroservices.config;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class KafkaConsumerConfiguration extends KafkaConfiguration {

    private final String keyDeserializer;
    private final String valueDeserializer;
    private final String offsetReset;
    private final Boolean enableAutoCommit;

    public KafkaConsumerConfiguration(final JsonObject configuration) {
        super(configuration);
        this.keyDeserializer = configuration.getString("kafka.key.deserializer");
        this.valueDeserializer = configuration.getString("kafka.value.deserializer");
        this.offsetReset = configuration.getString("kafka.offset.reset");
        this.enableAutoCommit = configuration.getBoolean("kafka.enable.auto.commit");
    }

    public Map<String, String> createConfig(final String consumerGroupId) {
        final Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", this.bootstrapServers);
        config.put("key.deserializer", this.keyDeserializer);
        config.put("value.deserializer", this.valueDeserializer);
        config.put("group.id", consumerGroupId);
        config.put("auto.offset.reset", this.offsetReset);

        if (Objects.nonNull(this.enableAutoCommit)) {
            config.put("enable.auto.commit", this.enableAutoCommit.toString());
        }

        return config;
    }
}
