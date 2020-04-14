package br.com.emmanuelneri.reactivemicroservices.cassandra.codec;

import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.extras.codecs.MappingCodec;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class LocalDateTimeCodec extends MappingCodec<LocalDateTime, Date> {

    public static final LocalDateTimeCodec instance = new LocalDateTimeCodec();

    private LocalDateTimeCodec() {
        super(TypeCodec.timestamp(), java.time.LocalDateTime.class);
    }

    @Override
    protected LocalDateTime deserialize(final Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    @Override
    protected Date serialize(final LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}