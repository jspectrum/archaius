package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import com.netflix.archaius.api.PropertySource;

public abstract class DelegatingPropertySource implements PropertySource {

    @Override
    public PropertySource snapshot() {
        return delegate().snapshot();
    }
    
    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public Optional<?> getProperty(String name) {
        return delegate().getProperty(name);
    }

    @Override
    public Collection<String> getKeys() {
        return delegate().getKeys();
    }

    @Override
    public Collection<String> getKeys(String prefix) {
        return delegate().getKeys(prefix);
    }
    
    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }
    
    @Override
    public int size() {
        return delegate().size();
    }

    @Override
    public AutoCloseable addListener(Consumer<PropertySource> consumer) {
        return delegate().addListener(consumer);
    }

    protected abstract PropertySource delegate();
}
