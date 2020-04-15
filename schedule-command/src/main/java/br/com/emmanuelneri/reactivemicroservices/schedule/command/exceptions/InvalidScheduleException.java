package br.com.emmanuelneri.reactivemicroservices.schedule.command.exceptions;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class InvalidScheduleException extends ValidationException {

    public InvalidScheduleException(final String message) {
        super(message);
    }

    @Override
    public InvalidMessage buildErrorMessage(final ConsumerRecord<?, ?> record) {
        return InvalidMessage.invalidBusinessValidation(record, this.getMessage());
    }
}
