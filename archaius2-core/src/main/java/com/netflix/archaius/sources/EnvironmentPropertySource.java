package com.netflix.archaius.sources;

import java.util.stream.Collectors;

public class EnvironmentPropertySource extends MapPropertySource {
    public EnvironmentPropertySource(String name) {
        super(name, System.getenv().entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
    }
}
