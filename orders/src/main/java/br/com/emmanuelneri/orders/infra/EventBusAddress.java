package br.com.emmanuelneri.orders.infra;

public enum EventBusAddress {

    RECEIVED_ORDER("received-order");

    private final String address;

    EventBusAddress(final String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
