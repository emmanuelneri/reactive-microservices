package br.com.emmanuelneri.commons.infra;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ConfigRetrieverConfiguration {

    public static ConfigRetriever configure(final Vertx vertx) {
        final ConfigStoreOptions store = new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setConfig(new JsonObject().put("path", "conf/config.properties"));

        return ConfigRetriever.create(vertx,
                new ConfigRetrieverOptions().addStore(store));
    }
}
