package br.com.emmanuelneri.reactivemicroservices.test;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class KafkaTestProducer {

    private final Vertx vertx;
    private final JsonObject configuration;

    public void send(final String topic, final String key, final String value) {
        final KafkaProducer<String, String> producer = createKafkaProducer();
        final KafkaProducerRecord<String, String> producerRecord = KafkaProducerRecord.create(topic, key, value);
        producer.send(producerRecord);
    }

    public void send(final String topic, final String key, final String value, final String headerKey, final String headerValue) {
        final KafkaProducer<String, String> producer = createKafkaProducer();
        final KafkaProducerRecord<String, String> producerRecord = KafkaProducerRecord.create(topic, key, value);
        producerRecord.addHeader(headerKey, headerValue);
        producer.send(producerRecord);
    }

    private KafkaProducer<String, String> createKafkaProducer() {
        final KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);
        return KafkaProducer.create(this.vertx, kafkaProducerConfiguration.createConfig());
    }
}
