package com.netflix.archaius.sources;

public class EnvironmentPropertySource {
    public static ImmutablePropertySource INSTANCE = ImmutablePropertySource.builder()
            .named("env")
            .putAll(System.getenv())
            .build();
}
