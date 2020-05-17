package br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces;

import br.com.emmanuelneri.reactivemicroservices.schedule.schema.RequestResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;

import static br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces.ScheduleProcessedWebSocketHandler.PROCESSED_RECEIVED_EVENT;

@RunWith(VertxUnitRunner.class)
public class ScheduleProcessedWebSocketHandlerTest {

    private static final int PORT = 9182;

    @Test
    public void shouldWriteOnWebSocketWhenScheduleProcessedReceived(final TestContext context) {
        final Vertx vertx = Vertx.vertx();

        final RequestResult result = new RequestResult();
        result.setSuccess(true);
        result.setRequestId(UUID.randomUUID().toString());

        final String resultAsString = Json.encode(result);

        final Handler<ServerWebSocket> webSocketHandler = ScheduleProcessedWebSocketHandler.create(vertx).getServerWebSocketHandler();
        final Async async = context.async();
        vertx.createHttpServer()
            .webSocketHandler(webSocketHandler)
            .listen(PORT, serverAsyncResult -> {
                if (serverAsyncResult.failed()) {
                    context.fail(serverAsyncResult.cause());
                }

                vertx.createHttpClient().webSocket(PORT, "localhost", "/", handler -> {
                    if (handler.failed()) {
                        context.fail(handler.cause());
                    }
                    vertx.eventBus().send(PROCESSED_RECEIVED_EVENT, resultAsString);
                    final WebSocket webSocket = handler.result();
                    webSocket.handler(data -> {
                        context.assertEquals(resultAsString, data.toString());
                        vertx.createHttpServer().close();
                        async.complete();
                    });
                });
            });
    }
}