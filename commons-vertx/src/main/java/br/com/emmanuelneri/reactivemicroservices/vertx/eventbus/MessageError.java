package br.com.emmanuelneri.reactivemicroservices.vertx.eventbus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageError {
    EXECUTION_ERROR(90),
    CONNECTION_ERROR(99);

    private final int code;

}
