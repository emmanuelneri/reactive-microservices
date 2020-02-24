package br.com.emmanuelneri.blueprint.schedule.connector.domain;

import io.vertx.core.json.Json;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public final class ProcessorResult {

    public static final ProcessorResult OK = new ProcessorResult(Status.OK, "");
    public static final ProcessorResult INTERNAL_ERROR = ProcessorResult.error("Internal error");
    public static final String OK_AS_JSON = Json.encode(OK);
    public static final String INTERNAL_ERROR_AS_JSON = Json.encode(INTERNAL_ERROR);

    public static ProcessorResult error(final String message) {
        return new ProcessorResult(Status.ERROR, message);
    }

    private Status status;
    private String message;

    public enum Status {
        OK, ERROR;
    }
}
