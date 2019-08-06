package br.com.emmanuelneri.payments.infra;

public enum Topic {

    ORDER("Order");

    private final String topic;

    Topic(final String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
}
