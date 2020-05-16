package br.com.emmanuelneri.reactivemicroservices.schedule.command;

import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.EventBusName;

public enum ScheduleCommandEvents implements EventBusName {

    SCHEDULE_RECEIVED,
    INVALID_SCHEDULE_RECEIVED,
    SCHEDULE_REQUEST_PROCESSED
    ;

    @Override
    public String getName() {
        return this.name();
    }
}
