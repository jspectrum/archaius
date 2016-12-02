package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import com.netflix.archaius.api.Cancellation;
import com.netflix.archaius.api.PropertySource;

public class MutablePropertySource implements PropertySource {
    private final Map<String, Object> properties;
    private final String name;
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

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
    public void forEach(String prefix, BiConsumer<String, Object> consumer) {
        if (!prefix.endsWith(".")) {
            forEach(prefix + ".", consumer);
        } else {
//            properties.subMap(prefix, prefix + Character.MAX_VALUE).forEach(consumer);
        }
    }

    @Override
    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public Cancellation addListener(Listener listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }
    
    protected void notifyListeners() {
        listeners.forEach(listener -> listener.onChanged(this));
    }
    
    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }
}
