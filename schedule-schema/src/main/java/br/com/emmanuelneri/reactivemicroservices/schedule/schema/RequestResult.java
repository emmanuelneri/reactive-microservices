package br.com.emmanuelneri.reactivemicroservices.schedule.schema;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class RequestResult {

    private String requestId;
    private boolean success;
    private String value;
    private String description;

    public RequestResult(final String requestId, final boolean success, final String value, final String description) {
        this.requestId = requestId;
        this.success = success;
        this.value = value;
        this.description = description;
    }
}