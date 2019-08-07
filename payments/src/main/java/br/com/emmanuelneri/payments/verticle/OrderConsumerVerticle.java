package br.com.emmanuelneri.payments.verticle;

import br.com.emmanuelneri.commons.infra.KafkaConfiguration;
import br.com.emmanuelneri.payments.infra.Topic;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumer;

import java.util.Map;

import static br.com.emmanuelneri.payments.infra.EventBusAddress.NEW_PAYMENT;


public class OrderConsumerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumerVerticle.class);
    private static final String CONSUMER_GROUP_ID = "ORDER_CONSUMER_GROUP";
    private static final String NEW_ORDER_TOPIC = Topic.ORDER.getTopic();

    private final KafkaConfiguration configuration;

    public OrderConsumerVerticle(final KafkaConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start(final Future<Void> startFuture) {
        final Map<String, String> config = configuration.createKafkaConsumerConfig(CONSUMER_GROUP_ID);
        final KafkaConsumer<String, String> kafkaConsumer = KafkaConsumer.create(vertx, config);

        kafkaConsumer.subscribe(NEW_ORDER_TOPIC, result -> {
           if(result.failed()) {
               LOGGER.error("failed to subscribe {0} topic", NEW_ORDER_TOPIC, result.cause());
               return;
           }

            LOGGER.info("topic {0} subscribed", NEW_ORDER_TOPIC);
        });

        kafkaConsumer.handler(result -> {
            LOGGER.info("message consumed {0}", result);

            final String order = result.value();
            vertx.eventBus().send(NEW_PAYMENT.getAddress(), order);
        });
    }
}
