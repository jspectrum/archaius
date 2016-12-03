package com.netflix.archaius.sources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;

import com.netflix.archaius.api.PropertySource;

/**
 * Immutable property source that is a composite of multiple {@link PropertySource}s.
 */
public class CompositePropertySource extends ImmutablePropertySource {
    private final List<PropertySource> sources;
    
    private static SortedMap<String, Supplier<Object>> joinSources(Collection<PropertySource> sources) {
        SortedMap<String, Supplier<Object>> values = new TreeMap<>();
        sources.forEach(source -> source.forEach((name, value) -> {
            values.putIfAbsent(name, value);
        }));
        return values;
    }
    
    public CompositePropertySource(String name, PropertySource... sources) {
        this(name, Arrays.asList(sources));
    }
    
    public CompositePropertySource(String name, Collection<PropertySource> sources) {
        super(name, joinSources(sources));
        this.sources = Collections.unmodifiableList(new ArrayList<>(sources));
    }
    
    public Collection<PropertySource> getSources() {
        return sources;
    }
}
