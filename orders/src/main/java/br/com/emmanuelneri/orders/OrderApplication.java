package br.com.emmanuelneri.orders;

import br.com.emmanuelneri.orders.infra.ConfigRetrieverConfiguration;
import br.com.emmanuelneri.orders.infra.Configuration;
import br.com.emmanuelneri.orders.verticle.OrderHttpServerVerticle;
import br.com.emmanuelneri.orders.verticle.OrderProducerVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.Vertx;

public class OrderApplication {

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();

        final ConfigRetriever configRetriever = ConfigRetrieverConfiguration.configure(vertx);

        configRetriever.getConfig(handler -> {
           final Configuration configuration = new Configuration(handler.result());

            vertx.deployVerticle(new OrderProducerVerticle(configuration));
            vertx.deployVerticle(new OrderHttpServerVerticle(configuration));
        });

    }

}
