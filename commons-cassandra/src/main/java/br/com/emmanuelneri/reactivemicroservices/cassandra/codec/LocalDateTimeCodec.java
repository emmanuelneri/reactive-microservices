package br.com.emmanuelneri.reactivemicroservices.cassandra.codec;

import br.com.emmanuelneri.reactivemicroservices.vertx.core.DateConfiguration;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.extras.codecs.MappingCodec;

import java.time.LocalDateTime;
import java.util.Date;

public class LocalDateTimeCodec extends MappingCodec<LocalDateTime, Date> {

    public static final LocalDateTimeCodec instance = new LocalDateTimeCodec();

    private LocalDateTimeCodec() {
        super(TypeCodec.timestamp(), java.time.LocalDateTime.class);
    }

    @Override
    protected LocalDateTime deserialize(final Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), DateConfiguration.DEFAULT_ZONE_ID);
    }

    @Override
    protected Date serialize(final LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(DateConfiguration.DEFAULT_ZONE_ID).toInstant());
    }
}