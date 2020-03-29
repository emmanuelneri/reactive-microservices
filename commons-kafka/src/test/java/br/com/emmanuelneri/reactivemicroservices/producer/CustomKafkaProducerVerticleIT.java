package br.com.emmanuelneri.reactivemicroservices.producer;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.EventBusName;
import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.ReplyResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class CustomKafkaProducerVerticleIT extends AbstractKafkaTest<String, String> {

    public static final EventBusName EVENT_BUS_NAME = () -> "custom-test-event-bus";
    public static final String TOPIC = "custom-test";

    @Test
    public void shouldProduceMessageWithEventBusReply(final TestContext context) {
        final KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);

        final Async async = context.async();
        this.vertx.deployVerticle(new CustomTestCase(kafkaProducerConfiguration), deployAsyncResult -> {
            if (deployAsyncResult.failed()) {
                context.fail(deployAsyncResult.cause());
                return;
            }

            final JsonObject test = new JsonObject()
                    .put("id", UUID.randomUUID().toString())
                    .put("name", "test");

            this.vertx.eventBus().request(EVENT_BUS_NAME.getName(), test, replyAsyncResult -> {
                if (replyAsyncResult.failed()) {
                    context.fail(replyAsyncResult.cause());
                    return;
                }

                Assert.assertEquals(ReplyResult.OK.asJson(), replyAsyncResult.result().body());
                final KafkaConsumer<String, String> kafkaConsumer = createKafkaConsumer(TOPIC);

                kafkaConsumer.handler(consumerRecord -> {
                    context.assertEquals(test.getString("id"), consumerRecord.key());
                    context.assertEquals(test.encode(), consumerRecord.value());

                    async.complete();
                });
            });
        });
    }
}

class CustomTestCase extends KafkaProducerVerticle<String, String> {

    public CustomTestCase(final KafkaProducerConfiguration kafkaProducerConfiguration) {
        super(kafkaProducerConfiguration);
    }

    @Override
    protected Handler<AsyncResult<Void>> afterProduceHandler(final Message<JsonObject> message) {
        return asyncResult -> {
            if (asyncResult.failed()) {
                message.reply(ReplyResult.INTERNAL_ERROR.asJson());
                return;
            }

            message.reply(ReplyResult.OK.asJson());
        };
    }

    @Override
    protected String topic() {
        return CustomKafkaProducerVerticleIT.TOPIC;
    }

    @Override
    protected EventBusName eventBusConsumer() {
        return CustomKafkaProducerVerticleIT.EVENT_BUS_NAME;
    }

    @Override
    protected String getKey(final JsonObject body) {
        return body.getString("id");
    }

    @Override
    protected String getValue(final JsonObject body) {
        return body.encode();
    }
}