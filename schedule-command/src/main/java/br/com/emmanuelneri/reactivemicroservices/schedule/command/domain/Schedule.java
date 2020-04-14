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
    private String description;

    private String customer;
    private String documentNumber;
    private String phone;
    private String email;
}
