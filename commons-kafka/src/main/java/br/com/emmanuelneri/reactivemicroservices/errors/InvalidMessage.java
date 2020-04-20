package br.com.emmanuelneri.reactivemicroservices.errors;

import io.vertx.core.json.Json;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@ToString
@NoArgsConstructor
public final class InvalidMessage {

    private String topic;
    private int partition;
    private long offset;
    private long timestamp;
    private String headers;
    private String key;
    private String value;
    private InvalidMessageReason reason;
    private String cause;

    public static InvalidMessage invalidDecodeValue(final ConsumerRecord<?, ?> consumerRecord, final String cause) {
        return new InvalidMessage(consumerRecord, InvalidMessageReason.VALUE_DECODE_FAILURE, cause);
    }

    public static InvalidMessage invalidBusinessValidation(final ConsumerRecord<?, ?> consumerRecord, final String cause) {
        return new InvalidMessage(consumerRecord, InvalidMessageReason.BUSINESS_VALIDATION_FAILURE, cause);
    }

    public static InvalidMessage unexpectedFailure(final ConsumerRecord<?, ?> consumerRecord, final Throwable cause) {
        return new InvalidMessage(consumerRecord, InvalidMessageReason.UNEXPECTED_FAILURE, cause.getMessage());
    }

    private InvalidMessage(final ConsumerRecord<?, ?> consumerRecord, final InvalidMessageReason reason, String cause) {
        this.topic = consumerRecord.topic();
        this.partition = consumerRecord.partition();
        this.offset = consumerRecord.offset();
        this.timestamp = consumerRecord.timestamp();
        this.headers = transformHeaders(consumerRecord.headers());
        this.key = Objects.nonNull(consumerRecord.key()) ? consumerRecord.key().toString() : null;
        this.value = consumerRecord.value().toString();
        this.reason = reason;
        this.cause = cause;
    }

    private String transformHeaders(final Headers headers) {
        return Json.encode(Stream.of(headers.toArray())
                .collect(Collectors.toMap(Header::key, header -> new String(header.value()))));
    }
}
