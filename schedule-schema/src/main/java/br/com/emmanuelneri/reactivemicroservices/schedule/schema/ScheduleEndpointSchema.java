package br.com.emmanuelneri.reactivemicroservices.schedule.schema;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ScheduleEndpointSchema {

    private LocalDateTime dateTime;
    private CustomerScheduleSchema customer;
    private String description;

}
