package br.com.emmanuelneri.orders;

import br.com.emmanuelneri.commons.infra.ConfigRetrieverConfiguration;
import br.com.emmanuelneri.commons.infra.HttpServerConfiguration;
import br.com.emmanuelneri.commons.infra.KafkaConfiguration;
import br.com.emmanuelneri.orders.verticle.OrderHttpServerVerticle;
import br.com.emmanuelneri.orders.verticle.OrderProducerVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.Vertx;

public class OrderApplication {

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();

        final ConfigRetriever configRetriever = ConfigRetrieverConfiguration.configure(vertx);

        configRetriever.getConfig(handler -> {
           final HttpServerConfiguration httpServerConfiguration = new HttpServerConfiguration(handler.result());
           final KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(handler.result());

            vertx.deployVerticle(new OrderProducerVerticle(kafkaConfiguration));
            vertx.deployVerticle(new OrderHttpServerVerticle(httpServerConfiguration));
        });

    }

}
