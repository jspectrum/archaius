package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.netflix.archaius.api.PropertySource;

public class SinglePropertySource implements PropertySource {
    private final String name;
    private final Optional<Object> value;
    
    public SinglePropertySource(String name, Object value) {
        this.name = name;
        this.value = Optional.of(value);
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Optional<Object> getProperty(String name) {
        if (this.name.equals(name)) {
            return value;
        }
        return Optional.empty();
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        consumer.accept(name, value.get());
    }

    @Override
    public Collection<String> getPropertyNames() {
        return Collections.singleton(name);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public PropertySource subset(String prefix) {
        return EmptyPropertySource.INSTANCE;
    }

    @Override
    public void addListener(Listener listener) {
    }

    @Override
    public void removeListener(Listener listener) {
    }
}
