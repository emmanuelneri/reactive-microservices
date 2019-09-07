package br.com.emmanuelneri.payments.verticle;

import br.com.emmanuelneri.commons.infra.KafkaConfiguration;
import br.com.emmanuelneri.commons.mapper.MapperBuilder;
import br.com.emmanuelneri.order.schema.OrderSchema;
import br.com.emmanuelneri.order.schema.OrderTopic;
import br.com.emmanuelneri.payments.domain.Order;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.modelmapper.ModelMapper;

import java.util.Map;

import static br.com.emmanuelneri.payments.infra.EventBusAddress.NEW_PAYMENT;

public class OrderConsumerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumerVerticle.class);
    private static final String CONSUMER_GROUP_ID = "ORDER_CONSUMER_GROUP";
    private static final String NEW_ORDER_TOPIC = OrderTopic.TOPIC.getName();

    private final KafkaConfiguration configuration;

    public OrderConsumerVerticle(final KafkaConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start(final Future<Void> startFuture) {
        final Map<String, String> config = configuration.createKafkaConsumerConfig(CONSUMER_GROUP_ID);
        final ModelMapper mapper = MapperBuilder.INSTANCE;
        final KafkaConsumer<String, String> kafkaConsumer = KafkaConsumer.create(vertx, config);

        kafkaConsumer.subscribe(OrderTopic.TOPIC.getName(), result -> {
            if (result.failed()) {
                LOGGER.error("failed to subscribe {0} topic", NEW_ORDER_TOPIC, result.cause());
                return;
            }

            LOGGER.info("topic {0} subscribed", NEW_ORDER_TOPIC);
        });

        kafkaConsumer.handler(result -> {
            LOGGER.info("message consumed {0}", result);

            final OrderSchema schema = Json.decodeValue(result.value(), OrderSchema.class);
            vertx.eventBus().send(NEW_PAYMENT.getAddress(), Json.encode(mapper.map(schema, Order.class)));
        });
    }
}
