package br.com.emmanuelneri.reactivemicroservices.schedule.command.exceptions;

import br.com.emmanuelneri.reactivemicroservices.errors.InvalidMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class InvalidScheduleSchemaException extends ValidationException {

    public InvalidScheduleSchemaException(final String message) {
        super(message);
    }

    @Override
    public InvalidMessage buildErrorMessage(final ConsumerRecord<?, ?> record) {
        return InvalidMessage.invalidDecodeValue(record, this.getMessage());
    }
}
