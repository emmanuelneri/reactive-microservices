package br.com.emmanuelneri.reactivemicroservices.schedule.schema;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ScheduleSchema {

    public static final String REQUEST_ID_HEADER = "request-id";

    private LocalDateTime dateTime;
    private CustomerSchema customer;
    private String description;
}
