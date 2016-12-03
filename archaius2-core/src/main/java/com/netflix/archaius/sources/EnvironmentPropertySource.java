package com.netflix.archaius.sources;

import java.util.TreeMap;
import java.util.stream.Collectors;

public class EnvironmentPropertySource extends ImmutablePropertySource {
    public EnvironmentPropertySource(String name) {
        super(name, System
            .getenv()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey(), 
                entry -> () -> entry.getValue(),
                (u, v) -> u, 
                TreeMap::new
                )));
    }
}
