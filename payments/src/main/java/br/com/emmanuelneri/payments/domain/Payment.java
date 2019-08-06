package br.com.emmanuelneri.payments.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class Payment {

    private String orderIdentifier;
    private BigDecimal value;
    private Status status;


    public static Payment fromOrder(final Order order) {
        return new Payment(order.getIdentifier(), order.getValue(), Status.OPEN);
    }

}
