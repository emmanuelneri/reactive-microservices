package br.com.emmanuelneri.blueprint.producer;

import br.com.emmanuelneri.blueprint.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.blueprint.vertx.eventbus.EventBusName;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.Map;

public abstract class KafkaProducerVerticle<Key, Value> extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerVerticle.class);

    private final Map<String, String> kafkaProducerConfiguration;

    public KafkaProducerVerticle(final KafkaProducerConfiguration kafkaProducerConfiguration) {
        this.kafkaProducerConfiguration = kafkaProducerConfiguration.createConfig();
    }

    @Override
    public void start() throws Exception {
        final KafkaProducer<Key, Value> kafkaProducer = KafkaProducer.create(this.vertx, this.kafkaProducerConfiguration);
        this.vertx.eventBus().<JsonObject>consumer(eventBusConsumer().getName(), message -> {
            final Promise<Void> promise = Promise.promise();
            send(kafkaProducer, message, promise);
            promise.future().setHandler(afterProduceHandler(message));
        });
    }

    private void send(final KafkaProducer<Key, Value> kafkaProducer,
                      final Message<JsonObject> message,
                      final Promise<Void> promise) {
        final JsonObject body = message.body();
        final KafkaProducerRecord<Key, Value> kafkaProducerRecord =
                KafkaProducerRecord.create(topic(), getKey(body), getValue(body));

        kafkaProducer.send(kafkaProducerRecord, result -> {
            if (result.failed()) {
                promise.fail(result.cause());
                return;
            }

            promise.complete();
        });
    }

    protected Handler<AsyncResult<Void>> afterProduceHandler(final Message<JsonObject> message) {
        return asyncResult -> {
            final JsonObject body = message.body();
            if (asyncResult.failed()) {
                LOGGER.error("message send error. topic: {0} - body: {1}",
                        topic(), body, asyncResult.cause());
                return;
            }

            LOGGER.info("message produced: topic: {0} - body: {1}",
                    topic(), body);
        };
    }

    protected abstract String topic();

    protected abstract EventBusName eventBusConsumer();

    protected abstract Key getKey(final JsonObject body);

    protected abstract Value getValue(final JsonObject body);

}
