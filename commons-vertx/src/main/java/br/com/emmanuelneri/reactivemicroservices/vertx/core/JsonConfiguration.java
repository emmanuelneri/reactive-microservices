package br.com.emmanuelneri.reactivemicroservices.vertx.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class JsonConfiguration {

    public static void setUpDefault() {
        DatabindCodec.mapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        DatabindCodec.mapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        DatabindCodec.mapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DatabindCodec.mapper().registerModule(new JavaTimeModule());
        DatabindCodec.mapper().setTimeZone(DateConfiguration.DEFAULT_TIME_ZONE);
    }
}
