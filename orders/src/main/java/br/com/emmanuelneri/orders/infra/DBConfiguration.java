package br.com.emmanuelneri.orders.infra;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public final class DBConfiguration {

    private final PgConnectOptions connectOptions;
    private final PoolOptions poolOptions;

    public DBConfiguration(final JsonObject configuration) {
        final String host = configuration.getString("db.host");
        final int port = configuration.getInteger("db.port");
        final String user = configuration.getString("db.user");
        final String password = configuration.getString("db.password");
        final String database = configuration.getString("db.database");
        final int poolMaxSize = configuration.getInteger("db.poolMaxSize");

        this.connectOptions = new PgConnectOptions()
                .setHost(host)
                .setPort(port)
                .setDatabase(database)
                .setUser(user)
                .setPassword(password);

        this.poolOptions = new PoolOptions()
                .setMaxSize(poolMaxSize);
    }

    public PgPool createPool(final Vertx vertx) {
        return PgPool.pool(vertx, connectOptions, poolOptions);
    }
}
