package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import com.netflix.archaius.api.PropertySource;

public class MutablePropertySource implements PropertySource {
    private final Map<String, Object> properties;
    private final String name;
    
    public MutablePropertySource(String name) {
        this.properties = new TreeMap<>();
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        properties.forEach(consumer);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public void addListener(Listener listener) {
        // TODO:
    }

    @Override
    public void removeListener(Listener listener) {
        // TODO:
    }

    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public PropertySource subset(String prefix) {
        // TODO:
        return null;
    }
}
