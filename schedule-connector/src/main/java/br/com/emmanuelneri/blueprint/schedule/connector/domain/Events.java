package br.com.emmanuelneri.blueprint.schedule.connector.domain;

import br.com.emmanuelneri.blueprint.vertx.eventbus.EventBusName;

public enum Events implements EventBusName {

    SCHEDULE_RECEIVED,
    SCHEDULE_VALIDATED;


    @Override
    public String getName() {
        return this.name();
    }
}
