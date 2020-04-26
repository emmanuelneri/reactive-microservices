package br.com.emmanuelneri.reactivemicroservices.schedule.command.test;

import br.com.emmanuelneri.reactivemicroservices.cassandra.test.CassandraTestConfiguration;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;
import org.testcontainers.containers.CassandraContainer;

@NoArgsConstructor(staticName = "create")
public class CassandraInit {

    public JsonObject start(final CassandraContainer cassandra) {
        final Cluster cluster = cassandra.getCluster();
        try (final Session session = cluster.connect()) {
            initKeyspace(session);
            createTable(session);
            return CassandraTestConfiguration.create().build(session);
        }
    }

    private void createTable(Session session) {
        session.execute("CREATE TABLE IF NOT EXISTS test.schedule " +
                "(data_time timestamp, description text, document_number text, customer text, phone text, email text, " +
                "PRIMARY KEY(data_time, description, document_number))");
    }

    private void initKeyspace(final Session session) {
        session.execute("CREATE KEYSPACE IF NOT EXISTS test WITH replication = " +
                "{'class':'SimpleStrategy','replication_factor':'1'};");

        session.execute("USE test");
    }
}
