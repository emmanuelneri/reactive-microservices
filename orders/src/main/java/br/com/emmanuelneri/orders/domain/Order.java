package br.com.emmanuelneri.orders.domain;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Order {

    private Long id;
    private String identifier;
    private BigDecimal value;

    public Order(final String identifier, final BigDecimal value) {
        this.identifier = identifier;
        this.value = value;
    }
}
