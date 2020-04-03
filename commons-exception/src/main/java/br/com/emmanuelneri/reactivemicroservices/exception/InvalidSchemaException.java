package br.com.emmanuelneri.reactivemicroservices.exception;

public class InvalidSchemaException extends BusinessException {

    public InvalidSchemaException(final String body) {
        super(String.format("invalid schema: %s", body));
    }
}
