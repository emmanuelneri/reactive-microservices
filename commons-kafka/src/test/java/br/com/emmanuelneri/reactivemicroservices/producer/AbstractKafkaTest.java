package br.com.emmanuelneri.reactivemicroservices.producer;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaConsumerConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.testcontainers.containers.KafkaContainer;

import java.util.Map;
import java.util.UUID;

@RunWith(VertxUnitRunner.class)
public abstract class AbstractKafkaTest<Key, Value> {

    @Rule
    public KafkaContainer kafka = new KafkaContainer("5.2.1");

    protected JsonObject configuration;
    protected Vertx vertx = Vertx.vertx();

    @Before
    public void before() {
        this.configuration = new JsonObject()
                .put("kafka.bootstrap.servers", kafka.getBootstrapServers())
                .put("kafka.key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                .put("kafka.value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                .put("kafka.key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                .put("kafka.value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                .put("kafka.offset.reset", "earliest");
    }

    protected KafkaConsumer<Key, Value> createKafkaConsumer(final String topic) {
        final Map<String, String> kafkaConsumerConfiguration = new KafkaConsumerConfiguration(this.configuration)
                .createConfig("kafka-test-consumer-" + UUID.randomUUID().toString());
        final KafkaConsumer<Key, Value> kafkaConsumer = KafkaConsumer.create(this.vertx, kafkaConsumerConfiguration);
        kafkaConsumer.subscribe(topic);
        return kafkaConsumer;
    }

}
