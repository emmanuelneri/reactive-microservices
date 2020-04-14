package br.com.emmanuelneri.reactivemicroservices.cassandra.config;

import io.vertx.cassandra.CassandraClientOptions;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class CassandraConfiguration {

    private final CassandraClientOptions options = new CassandraClientOptions();

    public CassandraConfiguration(final JsonObject configuration) {
        options.addContactPoint(configuration.getString("cassandra.contactPoint"));
        options.setPort(configuration.getInteger("cassandra.port"));
        options.setKeyspace(configuration.getString("cassandra.keyspace"));
    }
}
