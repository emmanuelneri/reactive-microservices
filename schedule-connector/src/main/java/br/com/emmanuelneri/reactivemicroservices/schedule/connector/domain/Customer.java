package br.com.emmanuelneri.reactivemicroservices.schedule.connector.domain;

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
