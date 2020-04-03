package br.com.emmanuelneri.reactivemicroservices.schedule.connector;

import br.com.emmanuelneri.reactivemicroservices.commons.config.ConfigRetrieverConfiguration;
import br.com.emmanuelneri.reactivemicroservices.commons.config.HttpServerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.mapper.JsonConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces.ScheduleEndpoint;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.usecase.ScheduleProcessor;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces.ScheduleProducer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class ScheduleConnectorApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleConnectorApplication.class);
    private static final String APPLICATION_NAME = "schedule-connector";

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();

        ConfigRetrieverConfiguration.configure(vertx, APPLICATION_NAME).getConfig(configurationHandler -> {
            if (configurationHandler.failed()) {
                LOGGER.error("configuration failed", configurationHandler.cause());
                return;
            }

            JsonConfiguration.setUpDefault();
            final JsonObject configuration = configurationHandler.result();
            final KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);
            final Router router = Router.router(vertx);

            vertx.deployVerticle(new ScheduleProducer(kafkaProducerConfiguration));
            vertx.deployVerticle(new ScheduleProcessor());
            vertx.deployVerticle(new ScheduleEndpoint(router));

            startHttpServer(vertx, configuration, router);
        });
    }

    private static void startHttpServer(final Vertx vertx, final JsonObject configuration, final Router router) {
        final HttpServerConfiguration httpServerConfiguration = new HttpServerConfiguration(configuration);

        final HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router)
                .listen(httpServerConfiguration.getPort(), asyncResult -> {
                    if (asyncResult.failed()) {
                        LOGGER.error("failed to start http server");
                        return;
                    }

                    LOGGER.info("http server started on port {0}", httpServer.actualPort());
                });
    }

}
