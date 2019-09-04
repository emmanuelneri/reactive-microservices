package br.com.emmanuelneri.order.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderSchema {
    private String identifier;
    private BigDecimal value;
}
