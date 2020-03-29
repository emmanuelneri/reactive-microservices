package br.com.emmanuelneri.blueprint.schedule.connector.domain;

import br.com.emmanuelneri.blueprint.exception.ValidationException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
public class Schedule {

    private LocalDateTime dateTime;
    private Customer customer;
    private String description;

    public void validate() {
        if (Objects.isNull(dateTime)) {
            throw new ValidationException("dateTime is required");
        }

        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("dateTime invalid. Past dateTime is not allowed");
        }

        if (Objects.isNull(description)) {
            throw new ValidationException("description is required");
        }

        if (Objects.isNull(customer)) {
            throw new ValidationException("customer is required");
        }

        if (Objects.isNull(customer.getDocumentNumber())) {
            throw new ValidationException("customer documentNumber is required");
        }

        if (Objects.isNull(customer.getName())) {
            throw new ValidationException("customer name is required");
        }

        if (Objects.isNull(customer.getPhone())) {
            throw new ValidationException("customer name is required");
        }
    }
}
