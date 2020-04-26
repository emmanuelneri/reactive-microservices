package br.com.emmanuelneri.reactivemicroservices.vertx.core;

import io.vertx.core.json.Json;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class VertxBuilderTest {

    @Test
    public void shouldEncodeLocalDateTime() {
        VertxBuilder.createAndConfigure();
        String encode = Json.encode(LocalDateTime.of(2020, 2, 1, 10, 20));
        Assert.assertEquals("\"2020-02-01T10:20:00\"", encode);
    }
}