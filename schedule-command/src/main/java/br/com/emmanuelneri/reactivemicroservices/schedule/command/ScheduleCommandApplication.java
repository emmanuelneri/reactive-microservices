package br.com.emmanuelneri.reactivemicroservices.schedule.command;

import br.com.emmanuelneri.reactivemicroservices.cassandra.config.CassandraConfiguration;
import br.com.emmanuelneri.reactivemicroservices.commons.config.ConfigRetrieverConfiguration;
import br.com.emmanuelneri.reactivemicroservices.config.KafkaConsumerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.config.KafkaProducerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleConsumerVerticle;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.SchedulePersistenceVerticle;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleRequestResultProducer;
import br.com.emmanuelneri.reactivemicroservices.vertx.core.VertxBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ScheduleCommandApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleCommandApplication.class);
    private static final String APPLICATION_NAME = "schedule-command";

    public static void main(final String[] args) {
        final Vertx vertx = VertxBuilder.createAndConfigure();

        ConfigRetrieverConfiguration.configure(vertx, APPLICATION_NAME).getConfig(configurationHandler -> {
            if (configurationHandler.failed()) {
                LOGGER.error("configuration failed", configurationHandler.cause());
                return;
            }

            start(vertx, configurationHandler.result());
        });
    }

    static void start(final Vertx vertx, final JsonObject configuration) {
        final KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);
        final KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(configuration);
        final CassandraConfiguration cassandraConfiguration = new CassandraConfiguration(configuration);


        vertx.deployVerticle(new ScheduleRequestResultProducer(kafkaProducerConfiguration));
        vertx.deployVerticle(new SchedulePersistenceVerticle(cassandraConfiguration));
        vertx.deployVerticle(new ScheduleConsumerVerticle(kafkaConsumerConfiguration));
    }
}