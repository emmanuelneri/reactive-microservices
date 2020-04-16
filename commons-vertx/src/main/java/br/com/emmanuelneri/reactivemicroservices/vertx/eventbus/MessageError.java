package br.com.emmanuelneri.reactivemicroservices.vertx.eventbus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageError {
    CONNECTION_ERROR(99);

    private final int code;

}
