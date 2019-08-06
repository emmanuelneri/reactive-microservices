package br.com.emmanuelneri.payments.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Order {

    private String identifier;
    private BigDecimal value;

}
