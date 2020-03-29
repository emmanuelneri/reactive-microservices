package br.com.emmanuelneri.reactivemicroservices.commons.config;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class HttpServerConfigurationTest {

    @Test
    public void serverPortPropertyShouldBeCustomPort() {
        final JsonObject configuration = new JsonObject();
        configuration.put(HttpServerConfiguration.SERVER_PORT_PROPERTY, 8090);

        final HttpServerConfiguration httpServerConfiguration = new HttpServerConfiguration(configuration);
        Assert.assertEquals(8090, httpServerConfiguration.getPort());
    }

    @Test
    public void defaultPortShouldBe8080() {
        final JsonObject configuration = new JsonObject();

        final HttpServerConfiguration httpServerConfiguration = new HttpServerConfiguration(configuration);
        Assert.assertEquals(8080, httpServerConfiguration.getPort());
    }
}