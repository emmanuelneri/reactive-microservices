package br.com.emmanuelneri.reactivemicroservices.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(final String message) {
        super(message);
    }
}
