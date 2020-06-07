package br.com.emmanuelneri.reactivemicroservices.commons.config;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ConfigRetrieverConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRetrieverConfiguration.class);
    private static final String KAFKA_BOOTSTRAP_SERVERS_ENV = "KAFKA_BOOTSTRAP_SERVERS";
    private static final String KAFKA_BOOTSTRAP_SERVERS_PROPERTY = "kafka.bootstrap.servers";
    private static final String CASSANDRA_CONTACT_POINTS_ENV = "CASSANDRA_CONTACT_POINTS";
    private static final String CASSANDRA_CONTACT_POINTS_PROPERTY = "cassandra.contactPoints";


    public static ConfigRetriever configure(final Environment environment, final Vertx vertx, final String applicationName) {
        LOGGER.info("environment: {0}", environment);

        if (environment == Environment.TEST) {
            return ConfigRetriever.create(vertx, new ConfigRetrieverOptions());
        }

        final String path = getFilePath(environment, applicationName);
        final ConfigStoreOptions fileStore = new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setConfig(new JsonObject().put("path", path));

        final ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);
        if (environment == Environment.DOCKER) {
            options.addStore(buildEnvStore());
        }

        return ConfigRetriever.create(vertx, options);
    }

    private static ConfigStoreOptions buildEnvStore() {
        final JsonObject configuration = new JsonObject();

        final String kafkaBootstrapEnvValue = System.getenv(KAFKA_BOOTSTRAP_SERVERS_ENV);
        if (kafkaBootstrapEnvValue != null) {
            configuration.put(KAFKA_BOOTSTRAP_SERVERS_PROPERTY, kafkaBootstrapEnvValue);
        }

        final String cassandraContactPointsValue = System.getenv(CASSANDRA_CONTACT_POINTS_ENV);
        if (cassandraContactPointsValue != null) {
            configuration.put(CASSANDRA_CONTACT_POINTS_PROPERTY, cassandraContactPointsValue);
        }

        return new ConfigStoreOptions()
                .setType("json")
                .setConfig(configuration);
    }

    static String getFilePath(final Environment environment, final String applicationName) {
        if (environment == Environment.DOCKER) {
            return String.format("/%s.properties", applicationName);
        }

        return String.format("%s/conf/%s.properties", applicationName, applicationName);
    }
}
