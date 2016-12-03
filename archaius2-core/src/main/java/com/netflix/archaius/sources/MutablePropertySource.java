package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.netflix.archaius.api.Cancellation;
import com.netflix.archaius.api.PropertySource;

public class MutablePropertySource implements PropertySource {
    private volatile SortedMap<String, Object> properties;
    private final String name;
    private final CopyOnWriteArrayList<Consumer<PropertySource>> listeners = new CopyOnWriteArrayList<>();

    public MutablePropertySource(String name) {
        this.properties = Collections.emptySortedMap();
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

    private void internalSetProperties(SortedMap<String, Object> newProperties) {
        this.properties = Collections.unmodifiableSortedMap(newProperties);
        notifyListeners();
    }
    
    public synchronized Object setProperty(String key, Object value) {
        SortedMap<String, Object> newProperties = new TreeMap<>(properties);
        Object oldValue = newProperties.put(key, value);
        
        if (oldValue == null || !oldValue.equals(value)) {
            internalSetProperties(newProperties);
        }
        return oldValue;
    }
    
    public synchronized void setProperties(Map<String, Object> values) {
        SortedMap<String, Object> newProperties = new TreeMap<>(properties);
        newProperties.putAll(values);
        internalSetProperties(newProperties);
    }
    
    public synchronized Object clearProperty(String key) {
        if (!properties.containsKey(key)) {
            return null;
        }
        
        SortedMap<String, Object> newProperties = new TreeMap<>(properties);
        Object oldValue = newProperties.remove(key);
        
        if (oldValue != null) {
            internalSetProperties(newProperties);
        }
        return oldValue;
    }

    public synchronized void clearProperties() {
        if (!properties.isEmpty()) {
            internalSetProperties(Collections.emptySortedMap());
        }
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
    public void forEach(String prefix, BiConsumer<String, Object> consumer) {
        if (!prefix.endsWith(".")) {
            forEach(prefix + ".", consumer);
        } else {
            properties
                .subMap(prefix, prefix + Character.MAX_VALUE)
                .forEach((key, value) -> consumer.accept(key.substring(prefix.length()), value));
        }
    }
    
    @Override
    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public Cancellation addListener(Consumer<PropertySource> consumer) {
        listeners.add(consumer);
        return () -> listeners.remove(consumer);
    }
    
    protected void notifyListeners() {
        listeners.forEach(listener -> listener.accept(this));
    }
    
    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }
}
