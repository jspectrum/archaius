package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.netflix.archaius.api.PropertySource;

public abstract class DelegatingPropertySource implements PropertySource {

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public Stream<Map.Entry<String, Object>> stream() {
        return delegate().stream();
    }
    
    @Override
    public Stream<Map.Entry<String, Object>> stream(String prefix) {
        return delegate().stream(prefix);
    }
    
    @Override
    public Optional<?> getProperty(String name) {
        return delegate().getProperty(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return delegate().getPropertyNames();
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public Runnable addListener(Consumer<PropertySource> consumer) {
        return delegate().addListener(consumer);
    }

    protected abstract PropertySource delegate();
}
