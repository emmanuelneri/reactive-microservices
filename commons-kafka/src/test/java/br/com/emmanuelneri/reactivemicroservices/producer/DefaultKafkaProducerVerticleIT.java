package br.com.emmanuelneri.reactivemicroservices.producer;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.EventBusName;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.junit.Test;

public class DefaultKafkaProducerVerticleIT extends AbstractKafkaTest<Long, String> {

    public static final EventBusName EVENT_BUS_NAME = () -> "test-event-bus";
    public static final String TOPIC = "test";

    @Test
    public void shouldProduceMessage(final TestContext context) {
        this.configuration
                .put("kafka.key.serializer", "org.apache.kafka.common.serialization.LongSerializer")
                .put("kafka.key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");

        final KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);

        final Async async = context.async();
        this.vertx.deployVerticle(new TestCase(kafkaProducerConfiguration), deployAsyncResult -> {
            if (deployAsyncResult.failed()) {
                context.fail(deployAsyncResult.cause());
                return;
            }

            final JsonObject test = new JsonObject()
                    .put("id", 1)
                    .put("name", "test");

            this.vertx.eventBus().send(EVENT_BUS_NAME.getName(), test);
            final KafkaConsumer<Long, String> kafkaConsumer = createKafkaConsumer(TOPIC);

            kafkaConsumer.handler(consumerRecord -> {
                context.assertEquals(test.getLong("id"), consumerRecord.key());
                context.assertEquals(test.encode(), consumerRecord.value());

                async.complete();
            });
        });
    }
}

class TestCase extends KafkaProducerVerticle<Long, String> {

    public TestCase(final KafkaProducerConfiguration kafkaProducerConfiguration) {
        super(kafkaProducerConfiguration);
    }

    @Override
    protected String topic() {
        return DefaultKafkaProducerVerticleIT.TOPIC;
    }

    @Override
    protected EventBusName eventBusConsumer() {
        return DefaultKafkaProducerVerticleIT.EVENT_BUS_NAME;
    }

    @Override
    protected Long getKey(final JsonObject body) {
        return body.getLong("id");
    }

    @Override
    protected String getValue(final JsonObject body) {
        return body.encode();
    }
}