package br.com.emmanuelneri.reactivemicroservices.errors;

import io.vertx.core.json.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Objects;

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
