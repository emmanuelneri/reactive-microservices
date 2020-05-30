package br.com.emmanuelneri.reactivemicroservices.vertx.core;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JsonConfigurationTest {

    @Test
    public void shouldJsonEncodeFromLocalDate() {
        JsonConfiguration.setUpDefault();
        String encode = Json.encode(LocalDate.of(2020, 2, 1));
        Assert.assertEquals("\"2020-02-01\"", encode);
    }

    @Test
    public void shouldJsonEncodeFromLocalDateTime() {
        JsonConfiguration.setUpDefault();
        String encode = Json.encode(LocalDateTime.of(2020, 2, 1, 10, 20));
        Assert.assertEquals("\"2020-02-01T10:20:00\"", encode);
    }

    @Test
    public void shouldJsonDecodeToLocalDate() {
        JsonConfiguration.setUpDefault();
        final LocalDate localDate = LocalDate.of(2020, 2, 1);
        Assert.assertEquals(localDate, Json.decodeValue("\"2020-02-01\"", LocalDate.class));
    }

    @Test
    public void shouldJsonDecodeToLocalDateTime() {
        JsonConfiguration.setUpDefault();
        final LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 1, 10, 20);
        Assert.assertEquals(localDateTime, Json.decodeValue("\"2020-02-01T10:20:00\"", LocalDateTime.class));
    }

    @Test
    public void shouldEncodeObjectWithLocalDateTimeToJsonObject() {
        JsonConfiguration.setUpDefault();

        final ObjectWitDate object = ObjectWitDate
                .builder()
                .name("LocalDateTime encode")
                .localDateTime(LocalDateTime.of(2020, 2, 1, 10, 20))
                .build();

        final JsonObject jsonObject = JsonObject.mapFrom(object);
        Assert.assertEquals("2020-02-01T10:20:00", jsonObject.getString("localDateTime"));
    }

    @Test
    public void shouldDecodeObjectWithLocalDateTimeToJsonObject() {
        JsonConfiguration.setUpDefault();

        final String objectAsString = "{\"name\":\"LocalDateTime encode\",\"localDateTime\":\"2020-02-01T10:20:00\"}";
        final ObjectWitDate object = new JsonObject(objectAsString).mapTo(ObjectWitDate.class);
        Assert.assertEquals(object.getLocalDateTime(), LocalDateTime.of(2020, 2, 1, 10, 20));
    }

    @Test
    public void shouldNotEncodeNullObjectFields() {
        JsonConfiguration.setUpDefault();

        final Schema schema = Schema.builder()
                .name("Schema")
                .build();

        final String expected = "{\"name\":\"Schema\",\"age\":0}";
        Assert.assertEquals(expected, Json.encode(schema));
    }

    @Test
    public void shouldDecodeJsonWithoutAllFields() {
        JsonConfiguration.setUpDefault();

        final String schemaAsJson = "{\"name\":\"Schema\",\"age\":0}";

        final Schema schema = Schema.builder()
                .name("Schema")
                .build();

        Assert.assertEquals(schema, Json.decodeValue(schemaAsJson, Schema.class));
    }

    @Test
    public void shouldNotFailOnUnknownFields() {
        JsonConfiguration.setUpDefault();

        final String schemaAsJson = "{\"name\":\"Schema\",\"age\":0,\"order\":0}";

        final Schema schema = Schema.builder()
                .name("Schema")
                .build();

        Assert.assertEquals(schema, Json.decodeValue(schemaAsJson, Schema.class));
    }

    @ToString
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ObjectWitDate {
        private String name;
        private LocalDateTime localDateTime;
    }

    @Getter
    @Setter
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Schema {
        private String name;
        private int age;
        private String email;
    }
}