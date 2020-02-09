package br.com.emmanuelneri.blueprint.mapper;
import io.vertx.core.json.Json;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JsonConfigurationTest {

    @Test
    public void shouldFormatLocalDate() {
        JsonConfiguration.setUpDefault();

        String encode = Json.encode(LocalDate.of(2020, 2, 1));
        Assert.assertEquals("\"2020-02-01\"", encode);
    }

    @Test
    public void shouldFormatLocalDateTime() {
        JsonConfiguration.setUpDefault();

        String encode = Json.encode(LocalDateTime.of(2020, 2, 1, 10, 20));
        Assert.assertEquals("\"2020-02-01T10:20:00\"", encode);
    }
}