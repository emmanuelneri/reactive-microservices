package br.com.emmanuelneri.order.schema;

import lombok.Value;

@Value
public final class OrderTopic {

    public static final OrderTopic TOPIC = new OrderTopic("Order");

    private final String name;
}
