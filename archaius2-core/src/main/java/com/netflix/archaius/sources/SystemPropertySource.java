package com.netflix.archaius.sources;

public class SystemPropertySource {
    public static ImmutablePropertySource INSTANCE = ImmutablePropertySource.builder()
            .named("system")
            .putAll(System.getProperties())
            .build();
}
