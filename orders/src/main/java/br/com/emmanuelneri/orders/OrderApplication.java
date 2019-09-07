package br.com.emmanuelneri.orders;

import br.com.emmanuelneri.commons.infra.ConfigRetrieverConfiguration;
import br.com.emmanuelneri.commons.infra.HttpServerConfiguration;
import br.com.emmanuelneri.commons.infra.KafkaConfiguration;
import br.com.emmanuelneri.orders.infra.DBConfiguration;
import br.com.emmanuelneri.orders.verticle.OrderHttpServerVerticle;
import br.com.emmanuelneri.orders.verticle.OrderProducerVerticle;
import br.com.emmanuelneri.orders.verticle.OrderStorageVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;

public class OrderApplication {

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();

        final ConfigRetriever configRetriever = ConfigRetrieverConfiguration.configure(vertx);

        configRetriever.getConfig(handler -> {
            final JsonObject config = handler.result();
            final HttpServerConfiguration httpServerConfiguration = new HttpServerConfiguration(config);
            final KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(config);
            final PgPool pgPool = new DBConfiguration(config).createPool(vertx);

            vertx.deployVerticle(new OrderProducerVerticle(kafkaConfiguration));
            vertx.deployVerticle(new OrderHttpServerVerticle(httpServerConfiguration));
            vertx.deployVerticle(new OrderStorageVerticle(pgPool));
        });

    }

}
