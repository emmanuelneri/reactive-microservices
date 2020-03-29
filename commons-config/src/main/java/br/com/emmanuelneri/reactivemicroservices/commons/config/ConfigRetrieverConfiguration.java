package br.com.emmanuelneri.reactivemicroservices.commons.config;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ConfigRetrieverConfiguration {

    public static ConfigRetriever configure(final Vertx vertx, final String applicationName) {
        final ConfigStoreOptions store = new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setConfig(new JsonObject().put("path", getFilePath(applicationName)));

        return ConfigRetriever.create(vertx,
                new ConfigRetrieverOptions().addStore(store));
    }

    static String getFilePath(final String applicationName) {
        return String.format("%s/conf/%s.properties", applicationName, applicationName);
    }
}
