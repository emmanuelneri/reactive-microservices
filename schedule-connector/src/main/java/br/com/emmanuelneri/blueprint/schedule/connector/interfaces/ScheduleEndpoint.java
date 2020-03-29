package br.com.emmanuelneri.blueprint.schedule.connector.interfaces;

import br.com.emmanuelneri.blueprint.commons.web.FailureHandler;
import br.com.emmanuelneri.blueprint.schedule.connector.domain.Events;
import br.com.emmanuelneri.blueprint.vertx.eventbus.ReplyResult;
import br.com.emmanuelneri.blueprint.vertx.eventbus.RetryResultStatus;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
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
            final Promise<ReplyResult> promise = Promise.promise();
            final String body = routingContext.getBodyAsString();
            LOGGER.info("schedule received {0}", body);

            processSchedule(body, promise);

            promise.future().setHandler(asyncResult -> {
                if (asyncResult.failed()) {
                    LOGGER.error("internal error", asyncResult.cause());
                    routingContext
                            .response()
                            .setStatusCode(500)
                            .setStatusMessage("internal error")
                            .end();
                    return;
                }

                final ReplyResult replyResult = asyncResult.result();
                routingContext
                        .response()
                        .setStatusCode(getHttpStatus(replyResult))
                        .end(replyResult.getMessage());
            });
        };
    }

    private void processSchedule(final String body, final Promise<ReplyResult> promise) {
        this.vertx.eventBus().<JsonObject>request(Events.SCHEDULE_RECEIVED.name(), body, async -> {
            if (async.failed()) {
                promise.fail(async.cause());
                return;
            }

            final ReplyResult replyResult = async.result().body().mapTo(ReplyResult.class);
            promise.complete(replyResult);
        });
    }

    private int getHttpStatus(final ReplyResult replyResult) {
        return replyResult.getStatus() == RetryResultStatus.OK
                ? HttpResponseStatus.CREATED.code()
                : HttpResponseStatus.BAD_REQUEST.code();
    }
}
