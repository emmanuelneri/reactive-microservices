package br.com.emmanuelneri.reactivemicroservices.schedule.command.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Schedule {

    private LocalDateTime dateTime;
    private Customer customer;
    private String description;

}
