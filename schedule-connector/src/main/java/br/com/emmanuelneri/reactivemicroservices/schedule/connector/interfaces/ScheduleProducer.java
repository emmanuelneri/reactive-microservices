package br.com.emmanuelneri.reactivemicroservices.schedule.connector.interfaces;

import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.producer.KafkaProducerVerticle;
import br.com.emmanuelneri.reactivemicroservices.schedule.connector.domain.Events;
import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.ReplyResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ScheduleProducer extends KafkaProducerVerticle<String, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleProducer.class);

    static final String SCHEDULE_REQUEST_TOPIC = "ScheduleRequested";

    public ScheduleProducer(final KafkaProducerConfiguration kafkaProducerConfiguration) {
        super(kafkaProducerConfiguration);
    }

    @Override
    protected Handler<AsyncResult<Void>> afterProduceHandler(final Message<JsonObject> message) {
        return asyncResult -> {
            final JsonObject body = message.body();

            if (asyncResult.failed()) {
                LOGGER.error("message send error. {0}", body, asyncResult.cause());
                message.reply(ReplyResult.INTERNAL_ERROR.asJson());
                return;
            }

            LOGGER.info("message produced: {0}", body);
            message.reply(ReplyResult.OK.asJson());
        };
    }

    @Override
    protected String topic() {
        return SCHEDULE_REQUEST_TOPIC;
    }

    @Override
    protected Events eventBusConsumer() {
        return Events.SCHEDULE_VALIDATED;
    }

    @Override
    protected String getKey(final JsonObject body) {
        return body
                .getJsonObject("customer")
                .getString("documentNumber");
    }

    @Override
    protected String getValue(final JsonObject body) {
        return body.encode();
    }
}
