package br.com.emmanuelneri.reactivemicroservices.schedule.schema;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ScheduleRequestResult {

    private String requestId;
    private boolean success;
    private String description;

    public ScheduleRequestResult(final String requestId, final boolean success, final String description) {
        this.requestId = requestId;
        this.success = success;
        this.description = description;
    }
}