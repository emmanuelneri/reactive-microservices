package br.com.emmanuelneri.reactivemicroservices.vertx.eventbus;

import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public final class ReplyResult {

    public static final ReplyResult OK = ReplyResult.ok();
    public static final ReplyResult INTERNAL_ERROR = ReplyResult.error("Internal error");
    private static final String EMPTY = "";

    public static ReplyResult ok() {
        return new ReplyResult(RetryResultStatus.OK, EMPTY);
    }

    public static ReplyResult error(final String message) {
        return new ReplyResult(RetryResultStatus.ERROR, message);
    }

    public JsonObject asJson() {
        return JsonObject.mapFrom(this);
    }

    private RetryResultStatus status;
    private String message;
}