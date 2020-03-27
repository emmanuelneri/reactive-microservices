package br.com.emmanuelneri.blueprint.schedule.connector.interfaces;

import br.com.emmanuelneri.blueprint.commons.web.FailureHandler;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.Events;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.ProcessorResult;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ScheduleEndpoint extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleEndpoint.class);
    private static final String PATH = "/schedules";

    private final Router router;

    @Override
    public void start(final Promise<Void> startFuture) throws Exception {
        final FailureHandler failureHandler = new FailureHandler();
        router.route().handler(BodyHandler.create()).failureHandler(failureHandler);
        router.post(PATH).handler(scheduleReceivedRoutingHandler()).failureHandler(failureHandler);
        startFuture.complete();
    }

    private Handler<RoutingContext> scheduleReceivedRoutingHandler() {
        return routingContext -> {
            LOGGER.info("schedule received {0}", routingContext.getBody());
            this.vertx.eventBus().request(Events.SCHEDULE_RECEIVED.name(), routingContext.getBody(), async -> {
                if (async.failed()) {
                    LOGGER.error("internal error", async.cause());
                    routingContext
                            .response()
                            .setStatusCode(500)
                            .setStatusMessage("internal error")
                            .end();
                    return;
                }

                final ProcessorResult processorResult = Json.decodeValue(async.result().body().toString(), ProcessorResult.class);
                routingContext
                        .response()
                        .setStatusCode(getHttpStatus(processorResult))
                        .end(processorResult.getMessage());
            });
        };
    }

    private int getHttpStatus(final ProcessorResult processorResult) {
        return processorResult.getStatus() == ProcessorResult.Status.OK
                ? HttpResponseStatus.ACCEPTED.code()
                : HttpResponseStatus.BAD_REQUEST.code();
    }
}
