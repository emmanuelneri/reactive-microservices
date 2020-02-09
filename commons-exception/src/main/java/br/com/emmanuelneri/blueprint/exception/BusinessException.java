package br.com.emmanuelneri.blueprint.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(final String message) {
        super(message);
    }
}
