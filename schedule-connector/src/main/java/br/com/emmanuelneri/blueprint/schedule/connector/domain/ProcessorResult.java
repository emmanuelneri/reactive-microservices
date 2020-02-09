package br.com.emmanuelneri.blueprint.schedule.connector.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public final class ProcessorResult {

    public static final ProcessorResult OK = new ProcessorResult(Status.OK, "");

    public static ProcessorResult error(final String message) {
        return new ProcessorResult(Status.ERROR, message);
    }

    private Status status;
    private String message;

    public enum Status {
        OK, ERROR;
    }
}
