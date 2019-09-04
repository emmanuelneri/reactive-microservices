package br.com.emmanuelneri.payments.mapper;

import br.com.emmanuelneri.order.schema.OrderSchema;
import br.com.emmanuelneri.payments.domain.Order;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderMapper {

    public static final OrderMapper INSTANCE = new OrderMapper();

    public Order schemaToOrder(final OrderSchema schema) {
        final Order order = new Order();
        order.setIdentifier(schema.getIdentifier());
        order.setValue(schema.getValue());
        return order;
    }
}
