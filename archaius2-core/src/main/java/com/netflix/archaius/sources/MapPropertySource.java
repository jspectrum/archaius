package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.netflix.archaius.api.PropertySource;

public class MapPropertySource implements PropertySource {
    protected final SortedMap<String, Object> properties;
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
        SortedMap<String, Object> data = new TreeMap<String, Object>();
        
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
        this.properties = new TreeMap<>(data);
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

    @Override
    public PropertySource subset(String prefix) {
        SortedMap<String, Object> subProperties = properties.subMap(prefix + ".", prefix + "." + Character.MAX_VALUE);
        
        return new PropertySource() {
            @Override
            public String getName() {
                return "mutable";
            }

            @Override
            public Optional<Object> getProperty(String name) {
                return Optional
                    .ofNullable(subProperties.get(prefix + "." + name));
            }

            @Override
            public void forEach(BiConsumer<String, Object> consumer) {
                subProperties
                    .forEach((key, value) -> consumer.accept(key.substring(prefix.length()+1), value));
            }

            @Override
            public Collection<String> getPropertyNames() {
                return subProperties
                    .keySet()
                    .stream()
                    .map(key -> key.substring(prefix.length()+1))
                    .collect(Collectors.toList());
            }

            @Override
            public boolean isEmpty() {
                return subProperties.isEmpty();
            }
            
            @Override
            public PropertySource subset(String childPrefix) {
                return subset(prefix + "." + childPrefix);
            }

            @Override
            public void addListener(Listener ignore) {
            }

            @Override
            public void removeListener(Listener ignore) {
            }
        };
    }

    @Override
    public void addListener(Listener ignore) {
    }

    @Override
    public void removeListener(Listener ignore) {
    }
}
