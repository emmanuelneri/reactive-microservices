package br.com.emmanuelneri.reactivemicroservices.schedule.connector;

import br.com.emmanuelneri.reactivemicroservices.commons.config.ConfigRetrieverConfiguration;
import br.com.emmanuelneri.reactivemicroservices.commons.config.HttpServerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.config.KafkaConsumerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces.ScheduleProcessedConsumer;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces.ScheduleProcessedWebSocketHandler;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces.ScheduleProducer;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces.ScheduleRequestEndpoint;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.usecase.ScheduleRequestProcessor;
import br.com.emmanuelneri.reactivemicroservices.vertx.core.VertxBuilder;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class ScheduleConnectorApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleConnectorApplication.class);
    private static final String APPLICATION_NAME = "schedule-connector";

    public static void main(String[] args) {
        final Vertx vertx = VertxBuilder.createAndConfigure();

        ConfigRetrieverConfiguration.configure(vertx, APPLICATION_NAME).getConfig(configurationHandler -> {
            if (configurationHandler.failed()) {
                LOGGER.error("configuration failed", configurationHandler.cause());
                return;
            }

            final JsonObject configuration = configurationHandler.result();
            final KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);
            final KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);
            final Router router = Router.router(vertx);

            vertx.deployVerticle(new ScheduleProcessedConsumer(kafkaConsumerConfiguration));
            vertx.deployVerticle(new ScheduleProducer(kafkaProducerConfiguration));
            vertx.deployVerticle(new ScheduleRequestProcessor());
            vertx.deployVerticle(new ScheduleRequestEndpoint(router));

            final Handler<ServerWebSocket> scheduleProcessedWebSockeHandler = ScheduleProcessedWebSocketHandler
                .create(vertx).getServerWebSocketHandler();

            startHttpServer(vertx, configuration, router, scheduleProcessedWebSockeHandler);
        });
    }

    private static void startHttpServer(final Vertx vertx, final JsonObject configuration, final Router router,
        final Handler<ServerWebSocket> handler) {
        final HttpServerConfiguration httpServerConfiguration = new HttpServerConfiguration(configuration);

        final HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router)
            .webSocketHandler(handler)
            .listen(httpServerConfiguration.getPort(), asyncResult -> {
                if (asyncResult.failed()) {
                    LOGGER.error("failed to start http server");
                    return;
                }

                LOGGER.info("http server started on port {0}", httpServer.actualPort());
            });
    }

}
