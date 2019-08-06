package br.com.emmanuelneri.payments;

import br.com.emmanuelneri.commons.infra.ConfigRetrieverConfiguration;
import br.com.emmanuelneri.commons.infra.KafkaConfiguration;
import br.com.emmanuelneri.payments.verticle.OrderConsumerVerticle;
import br.com.emmanuelneri.payments.verticle.PaymentConsumerVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.Vertx;

public class PaymentApplication {

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();

        final ConfigRetriever configRetriever = ConfigRetrieverConfiguration.configure(vertx);

        configRetriever.getConfig(handler -> {
            final KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(handler.result());

            vertx.deployVerticle(new OrderConsumerVerticle(kafkaConfiguration));
            vertx.deployVerticle(new PaymentConsumerVerticle());


        });

    }


}
