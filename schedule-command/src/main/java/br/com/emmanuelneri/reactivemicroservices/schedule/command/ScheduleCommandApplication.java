package br.com.emmanuelneri.reactivemicroservices.schedule.command;

import br.com.emmanuelneri.reactivemicroservices.cassandra.config.CassandraConfiguration;
import br.com.emmanuelneri.reactivemicroservices.commons.config.ConfigRetrieverConfiguration;
import br.com.emmanuelneri.reactivemicroservices.config.KafkaConsumerConfiguration;
import br.com.emmanuelneri.reactivemicroservices.mapper.JsonConfiguration;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.ScheduleConsumerVerticle;
import br.com.emmanuelneri.reactivemicroservices.schedule.command.interfaces.SchedulePersistenceVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ScheduleCommandApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleCommandApplication.class);
    private static final String APPLICATION_NAME = "schedule-command";

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();

        ConfigRetrieverConfiguration.configure(vertx, APPLICATION_NAME).getConfig(configurationHandler -> {
            if (configurationHandler.failed()) {
                LOGGER.error("configuration failed", configurationHandler.cause());
                return;
            }

            JsonConfiguration.setUpDefault();
            final JsonObject configuration = configurationHandler.result();
            final KafkaConsumerConfiguration kafkaConsumerConfiguration = new KafkaConsumerConfiguration(configuration);
            final CassandraConfiguration cassandraConfiguration = new CassandraConfiguration(configuration);


            vertx.deployVerticle(new ScheduleConsumerVerticle(kafkaConsumerConfiguration));
            vertx.deployVerticle(new SchedulePersistenceVerticle(cassandraConfiguration));
        });
    }
}