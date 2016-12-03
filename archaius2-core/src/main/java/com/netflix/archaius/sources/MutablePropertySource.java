package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.internal.GarbageCollectingSet;

public class MutablePropertySource implements PropertySource {
    private volatile SortedMap<String, Supplier<Object>> properties;
    private final String name;
    private final GarbageCollectingSet<Consumer<PropertySource>> listeners = new GarbageCollectingSet<>();

    public MutablePropertySource(String name) {
        this.properties = Collections.emptySortedMap();
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

    private void internalSetProperties(SortedMap<String, Supplier<Object>> newProperties) {
        this.properties = Collections.unmodifiableSortedMap(newProperties);
        notifyListeners();
    }
    
    public synchronized Object setProperty(String key, Object value) {
        SortedMap<String, Supplier<Object>> newProperties = new TreeMap<>(properties);
        Object oldValue = newProperties.put(key, () -> value);
        
        if (oldValue == null || !oldValue.equals(value)) {
            internalSetProperties(newProperties);
        }
        return oldValue;
    }
    
    public synchronized void setProperties(Map<String, Object> values) {
        SortedMap<String, Supplier<Object>> newProperties = new TreeMap<>(properties);
        values.forEach((k, v) -> newProperties.put(k, () -> v));
        internalSetProperties(newProperties);
    }
    
    public synchronized Object clearProperty(String key) {
        if (!properties.containsKey(key)) {
            return null;
        }
        
        SortedMap<String, Supplier<Object>> newProperties = new TreeMap<>(properties);
        Object oldValue = newProperties.remove(key);
        internalSetProperties(newProperties);
        return oldValue;
    }

    public synchronized void clearProperties() {
        if (!properties.isEmpty()) {
            internalSetProperties(Collections.emptySortedMap());
        }
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return Optional.ofNullable(properties.get(name)).map(sv -> sv.get());
    }

    @Override
    public void forEach(BiConsumer<String, Supplier<Object>> consumer) {
        properties.forEach(consumer);
    }

    @Override
    public void forEach(String prefix, BiConsumer<String, Supplier<Object>> consumer) {
        if (!prefix.endsWith(".")) {
            forEach(prefix + ".", consumer);
        } else {
            properties
                .subMap(prefix, prefix + Character.MAX_VALUE)
                .forEach((k, sv) -> consumer.accept(k.substring(prefix.length()), sv));
        }
    }
    
    @Override
    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public Runnable addListener(Consumer<PropertySource> consumer) {
        return listeners.add(consumer, this);
    }
    
    protected void notifyListeners() {
        listeners.forEach(listener -> listener.accept(this));
    }
    
    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }
}
