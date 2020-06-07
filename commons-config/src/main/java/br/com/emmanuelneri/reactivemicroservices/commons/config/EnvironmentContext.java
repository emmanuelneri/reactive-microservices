package br.com.emmanuelneri.reactivemicroservices.commons.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnvironmentContext {

    private static final Environment DEFAULT = Environment.DEV;

    public static Environment getEnvironment() {
        final String environment = System.getenv("ENVIRONMENT");
        return environment != null ? Environment.valueOf(environment) : DEFAULT;
    }

}
