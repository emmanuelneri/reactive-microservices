package br.com.emmanuelneri.reactivemicroservices.errors;

import io.vertx.core.json.Json;
import lombok.Getter;
import lombok.ToString;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Objects;

@Getter
@ToString
public final class InvalidMessage {

    private final String topic;
    private final int partition;
    private final long offset;
    private final long timestamp;
    private final String headers;
    private final String key;
    private final String value;
    private final InvalidMessageReason reason;
    private final String cause;

    public static InvalidMessage invalidDecodeValue(final ConsumerRecord<?, ?> consumerRecord, final String cause) {
        return new InvalidMessage(consumerRecord, InvalidMessageReason.VALUE_DECODE_FAILURE, cause);
    }

    private InvalidMessage(final ConsumerRecord<?, ?> consumerRecord, final InvalidMessageReason reason, String cause) {
        this.topic = consumerRecord.topic();
        this.partition = consumerRecord.partition();
        this.offset = consumerRecord.offset();
        this.timestamp = consumerRecord.timestamp();
        this.headers = Json.encode(consumerRecord.headers());
        this.key = Objects.nonNull(consumerRecord.key()) ? consumerRecord.key().toString() : null;
        this.value = consumerRecord.value().toString();
        this.reason = reason;
        this.cause = cause;
    }
}
