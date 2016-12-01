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
import com.netflix.archaius.internal.Preconditions;

/**
 * Immutable PropertySource with a builder for conveniently creating a property source
 */
public class ImmutablePropertySource implements PropertySource {
    protected SortedMap<String, Object> properties;
    protected String name;
    
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
        ImmutablePropertySource source = new ImmutablePropertySource();
        
        public Builder named(String name) {
            Preconditions.checkArgument(source != null, "Builder already created");
            source.name = name;
            return this;
        }
        
        public <T> Builder put(String key, T value) {
            Preconditions.checkArgument(source != null, "Builder already created");
            source.properties.put(key, value.toString());
            return this;
        }
        
        public <T> Builder putAll(Map<String, Object> values) {
            Preconditions.checkArgument(source != null, "Builder already created");
            source.properties.putAll(values);
            return this;
        }
        
        public <T> Builder putIfAbsent(String key, T value) {
            Preconditions.checkArgument(source != null, "Builder already created");
            source.properties.putIfAbsent(key, value);
            return null;
        }

        public ImmutablePropertySource build() {
            try {
                if (source.name == null) {
                    source.name = "map-" + counter.incrementAndGet();
                }
                return source;
            } finally {
                source = null;
            }
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    private ImmutablePropertySource() {
        this(null, new TreeMap<>());
    }
    
    protected ImmutablePropertySource(String name, SortedMap<String, Object> values) {
        this.name = name;
        this.properties = values;
    }
    
    protected ImmutablePropertySource(String name, Map<String, Object> values) {
        this.name = name;
        this.properties = new TreeMap<>(values);
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
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public PropertySource subset(String prefix) {
        SortedMap<String, Object> subProperties = properties.subMap(prefix + ".", prefix + "." + Character.MAX_VALUE);
        
        return new PropertySource() {
            @Override
            public String getName() {
                return name;
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
                return ImmutablePropertySource.this.subset(prefix + "." + childPrefix);
            }
        };
    }
}
