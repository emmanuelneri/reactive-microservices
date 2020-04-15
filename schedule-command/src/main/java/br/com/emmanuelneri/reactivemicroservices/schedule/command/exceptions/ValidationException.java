package br.com.emmanuelneri.reactivemicroservices.schedule.command.exceptions;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public abstract class ValidationException extends RuntimeException {

    public ValidationException(final String message) {
        super(message);
    }

    public abstract InvalidMessage buildErrorMessage(final ConsumerRecord<?, ?> consumerRecord);
}
