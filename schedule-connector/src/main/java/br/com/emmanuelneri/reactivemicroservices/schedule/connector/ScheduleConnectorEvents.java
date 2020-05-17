package br.com.emmanuelneri.reactivemicroservices.schedule.connector;

import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.EventBusName;

public enum ScheduleConnectorEvents implements EventBusName {

    SCHEDULE_RECEIVED,
    SCHEDULE_VALIDATED,
    SCHEDULE_PROCESSED_RECEIVED;


    @Override
    public String getName() {
        return this.name();
    }
}
