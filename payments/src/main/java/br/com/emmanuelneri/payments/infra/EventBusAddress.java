package br.com.emmanuelneri.payments.infra;

import lombok.Getter;

@Getter
public enum EventBusAddress {

    NEW_PAYMENT("new-payment");

    private final String address;

    EventBusAddress(final String address) {
        this.address = address;
    }
}
