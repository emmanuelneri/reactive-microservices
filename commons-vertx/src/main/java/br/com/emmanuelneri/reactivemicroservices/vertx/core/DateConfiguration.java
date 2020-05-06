package br.com.emmanuelneri.reactivemicroservices.vertx.core;

import java.time.ZoneId;
import java.util.TimeZone;

public interface DateConfiguration {
    ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC");
    TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone(DEFAULT_ZONE_ID);
}
