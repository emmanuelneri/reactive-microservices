package br.com.emmanuelneri.blueprint.commons.config;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ConfigRetrieverConfigurationTest {

    @Test
    public void wrongApplicationNameShouldFailTest(final TestContext context) {
        final Vertx vertx = Vertx.vertx();

        ConfigRetrieverConfiguration.configure(vertx, "commons-config")
                .getConfig(configurationHandler -> context.asyncAssertFailure());
    }

    @Test
    public void applicationNameShouldReturnConfiguration(final TestContext context) {
        final Vertx vertx = Vertx.vertx();

        ConfigRetrieverConfiguration.configure(vertx, "schedule-configuration")
                .getConfig(configurationHandler -> context.asyncAssertSuccess());
    }
}