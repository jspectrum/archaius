package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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
     * ImmutablePropertySource.builder()
     *      .put("foo", "bar")
     *      .put("baz", 123)
     *      .build()
     * </pre>
     * }
     */
    public static class Builder {
        ImmutablePropertySource source = new ImmutablePropertySource("", new TreeMap<>());
        
        public Builder named(String name) {
            Preconditions.checkArgument(source != null, "Builder already created");
            source.name = name;
            return this;
        }
        
        public <T> Builder put(String key, T value) {
            Preconditions.checkArgument(source != null, "Builder already created");
            source.properties.put(key, value);
            return this;
        }
        
        public <T> Builder putIfAbsent(String key, T value) {
            Preconditions.checkArgument(source != null, "Builder already created");
            source.properties.putIfAbsent(key, value);
            return null;
        }

        public Builder putAll(Map<String, ?> values) {
            Preconditions.checkArgument(source != null, "Builder already created");
            values.forEach((k, v) -> source.properties.put(k, v));
            return this;
        }
        
        public Builder putAll(Properties props) {
            props.forEach((key, value) -> source.properties.put(key.toString(), value));
            return this;
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
    
    public ImmutablePropertySource(String name, SortedMap<String, Object> values) {
        this.name = name;
        this.properties = values;
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
    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public Stream<Entry<String, Object>> stream() {
        return properties.entrySet().stream();
    }

    @Override
    public Stream<Entry<String, Object>> stream(String prefix) {
        if (!prefix.endsWith(".")) {
            return stream(prefix + ".");
        } else {
            return properties.subMap(prefix, prefix + Character.MAX_VALUE)
                .entrySet()
                .stream()
                .map(PropertySourceUtils.stripPrefix(prefix));
        }
    }
}
