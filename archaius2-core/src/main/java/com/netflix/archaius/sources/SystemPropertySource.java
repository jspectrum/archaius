package com.netflix.archaius.sources;

import java.util.stream.Collectors;

public class SystemPropertySource extends MapPropertySource {
    public SystemPropertySource(String name) {
        super(name, System.getProperties()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().toString(), 
                entry -> entry.getValue())));
    }
}
