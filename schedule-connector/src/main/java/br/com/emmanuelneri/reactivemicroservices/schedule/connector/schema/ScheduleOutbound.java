package br.com.emmanuelneri.reactivemicroservices.schedule.connector.schema;

import br.com.emmanuelneri.reactivemicroservices.schedule.schema.ScheduleSchema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleOutbound {

    private ScheduleSchema schema;
    private UUID requestId;

    public String getKey() {
        return schema.getCustomer().getDocumentNumber();
    }
}