package br.com.emmanuelneri.reactivemicroservices.schedule.connector.domain;

import br.com.emmanuelneri.reactivemicroservices.vertx.eventbus.EventBusName;

public enum Events implements EventBusName {

    SCHEDULE_RECEIVED,
    SCHEDULE_VALIDATED;


    @Override
    public String getName() {
        return this.name();
    }
}
