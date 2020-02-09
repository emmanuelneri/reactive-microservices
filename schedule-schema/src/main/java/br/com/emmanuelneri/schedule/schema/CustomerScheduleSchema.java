package br.com.emmanuelneri.schedule.schema;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CustomerScheduleSchema {

    private String name;
    private String documentNumber;
    private String phone;
    private String email;

}
