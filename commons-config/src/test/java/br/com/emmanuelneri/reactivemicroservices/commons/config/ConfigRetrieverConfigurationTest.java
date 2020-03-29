package br.com.emmanuelneri.reactivemicroservices.commons.config;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ConfigRetrieverConfigurationTest {

    @Test
    public void wrongApplicationNameShouldFailTest(final TestContext context) {
        final Vertx vertx = Vertx.vertx();

        final Async async = context.async();
        ConfigRetrieverConfiguration.configure(vertx, "test")
                .getConfig(configurationHandler -> {
                    context.assertTrue(configurationHandler.failed());
                    async.complete();
                });
    }

    @Test
    public void applicationNameShouldReturnConfiguration(final TestContext context) {
        final String filePath = ConfigRetrieverConfiguration.getFilePath("commons-config");
        Assert.assertEquals("commons-config/conf/commons-config.properties", filePath);
    }
}