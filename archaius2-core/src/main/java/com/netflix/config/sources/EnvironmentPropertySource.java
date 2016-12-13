package com.netflix.config.sources;

public class EnvironmentPropertySource {
    public static ImmutablePropertySource INSTANCE = ImmutablePropertySource.builder()
            .named("env")
            .putAll(System.getenv())
            .build();
}
