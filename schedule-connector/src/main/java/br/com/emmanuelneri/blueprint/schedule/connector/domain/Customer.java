package br.com.emmanuelneri.blueprint.schedule.connector.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Customer {

    private String name;
    private String documentNumber;
    private String phone;
    private String email;

}
