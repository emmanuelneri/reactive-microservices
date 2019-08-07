package br.com.emmanuelneri.payments.verticle;

import br.com.emmanuelneri.payments.domain.Order;
import br.com.emmanuelneri.payments.domain.Payment;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import static br.com.emmanuelneri.payments.infra.EventBusAddress.NEW_PAYMENT;

public class PaymentConsumerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentConsumerVerticle.class);

    @Override
    public void start(final Future<Void> startFuture) {
        vertx.eventBus().localConsumer(NEW_PAYMENT.getAddress(), message -> {
            final Order order = Json.decodeValue(message.body().toString(), Order.class);
            final Payment payment = Payment.fromOrder(order);

            LOGGER.info("new payment {0}", payment);
        });
    }
}
