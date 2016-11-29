package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import com.netflix.archaius.api.PropertySource;

public class MapPropertySource implements PropertySource {
    protected final Map<String, Object> properties;
    protected final String name;
    
    private static final AtomicInteger counter = new AtomicInteger();

    /**
     * The builder only provides convenience for fluent style adding of properties
     * 
     * {@code
     * <pre>
     * MapConfig.builder()
     *      .put("foo", "bar")
     *      .put("baz", 123)
     *      .build()
     * </pre>
     * }
     */
    public static class Builder {
        Map<String, Object> data = new HashMap<String, Object>();
        
        public <T> Builder put(String key, T value) {
            data.put(key, value.toString());
            return this;
        }
        
        public MapPropertySource build() {
            return new MapPropertySource(data);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public MapPropertySource(Map<String, Object> data) {
        this("map-" + counter.incrementAndGet(), data);
    }
    
    public MapPropertySource(String name, Map<String, Object> data) {
        this.name = name;
        this.properties = new HashMap<>(data);
    }
    
    @Override
    public String getName() {
        return "mutable";
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
    public boolean isEmpty() {
        return properties.isEmpty();
    }
}
