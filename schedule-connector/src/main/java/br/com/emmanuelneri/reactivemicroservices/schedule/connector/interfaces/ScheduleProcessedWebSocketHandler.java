package br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces;

import br.com.emmanuelneri.reactivemicroservices.schedule.connector.ScheduleConnectorEvents;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ScheduleProcessedWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleProcessedWebSocketHandler.class);
    static final String PROCESSED_RECEIVED_EVENT = ScheduleConnectorEvents.SCHEDULE_PROCESSED_RECEIVED.getName();

    private final Handler<ServerWebSocket> serverWebSocketHandler;

    public static ScheduleProcessedWebSocketHandler create(final Vertx vertx) {
        return new ScheduleProcessedWebSocketHandler(vertx);
    }

    private ScheduleProcessedWebSocketHandler(final Vertx vertx) {
        this.serverWebSocketHandler = serverWebSocket -> {
            LOGGER.info("websocket started.");

            vertx.eventBus().consumer(PROCESSED_RECEIVED_EVENT, message -> {
                serverWebSocket.writeTextMessage(message.body().toString(), webSocketHandler -> {
                    if (webSocketHandler.failed()) {
                        LOGGER.error("failed to write event to webSocket server", webSocketHandler.cause());
                        return;
                    }

                    LOGGER.info("websocket writed. {0}", message.body());
                });
            });
        };
    }

    public Handler<ServerWebSocket> getServerWebSocketHandler() {
        return serverWebSocketHandler;
    }
}
