package br.com.emmanuelneri.reactivemicroservices.test;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaConsumerConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor(staticName = "create")
public class KafkaTestConsumer {

    private final Vertx vertx;
    private final JsonObject configuration;

    public KafkaConsumer<String, String> build(final String topic) {
        final KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);
        final KafkaConsumer<String, String> kafkaConsumer = KafkaConsumer.create(this.vertx, kafkaConsumerConfiguration.createConfig(UUID.randomUUID().toString()));
        kafkaConsumer.subscribe(topic);

        return kafkaConsumer;
    }
}
