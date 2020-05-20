package br.com.emmanuelneri.reactivemicroservices.cassandra.config;

import io.vertx.cassandra.CassandraClientOptions;
import io.vertx.core.json.JsonObject;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class CassandraConfiguration {

    private static final String CONTACT_POINT_SEPARATION_CHARACTER = ",";
    private static final String PORT_SEPARATION_CHARACTER = ":";
    private final CassandraClientOptions options = new CassandraClientOptions();

    public CassandraConfiguration(final JsonObject configuration) {
        final String contactPoints = configuration.getString("cassandra.contactPoints");
        if (Objects.nonNull(contactPoints)) {
            Stream.of(contactPoints.split(CONTACT_POINT_SEPARATION_CHARACTER))
                .forEach(contactPoint -> {
                    final int separationIndex = contactPoint.indexOf(":");
                    final String address = contactPoint.substring(0, separationIndex);
                    final int port = Integer.parseInt(contactPoint.substring(separationIndex + 1));
                    options.dataStaxClusterBuilder()
                        .addContactPointsWithPorts(new InetSocketAddress(address, port));
                });
        }
        else {
            options.setPort(configuration.getInteger("cassandra.port"));
            options.addContactPoint(configuration.getString("cassandra.contactPoint"));
        }

        options.setKeyspace(configuration.getString("cassandra.keyspace"));
    }
}
