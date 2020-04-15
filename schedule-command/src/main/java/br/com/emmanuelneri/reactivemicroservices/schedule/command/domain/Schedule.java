package br.com.emmanuelneri.reactivemicroservices.schedule.command.domain;

import br.com.emmanuelneri.reactivemicroservices.schedule.command.exceptions.InvalidScheduleException;
import com.google.common.base.Strings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
public class Schedule {

    private LocalDateTime dateTime;
    private String description;

    private String customer;
    private String documentNumber;
    private String phone;
    private String email;

    public void validate(final Handler<AsyncResult<Void>> handler) {
        final List<String> errors = new LinkedList<>();

        if (Strings.isNullOrEmpty(description)) {
            errors.add("description is required ");
        }

        if (Objects.isNull(dateTime)) {
            errors.add("dateTime is required");
        }

        if (Strings.isNullOrEmpty(documentNumber)) {
            errors.add("documentNumber is required");
        }

        if (errors.isEmpty()) {
            handler.handle(Future.succeededFuture());
        } else {
            final String errorMessage = String.join(", ", errors);
            handler.handle(Future.failedFuture(new InvalidScheduleException(errorMessage)));
        }
    }
}
