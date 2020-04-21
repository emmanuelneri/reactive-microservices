package br.com.emmanuelneri.reactivemicroservices.cassandra.test;

import com.datastax.driver.core.Session;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class CassandraTestConfiguration {

    public static final String DEFAULT_KEYSPACE = "test";

    public JsonObject build(final Session session) {
        final String address = session.getCluster().getMetadata().getAllHosts().stream().findAny().get().toString();
        final String contactPoint = address.substring(address.indexOf("/") + 1, address.indexOf(":"));
        final String port = address.substring(address.lastIndexOf(":") + 1);

        return new JsonObject()
                .put("cassandra.contactPoint", contactPoint)
                .put("cassandra.port", Integer.valueOf(port))
                .put("cassandra.keyspace", DEFAULT_KEYSPACE);
    }
}