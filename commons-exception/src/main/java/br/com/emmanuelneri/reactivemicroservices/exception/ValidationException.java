package br.com.emmanuelneri.reactivemicroservices.exception;

public class ValidationException extends BusinessException {

    public ValidationException(final String message) {
        super(message);
    }
}
