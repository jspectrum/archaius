package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.netflix.archaius.api.PropertySource;

public abstract class DelegatingPropertySource implements PropertySource {

    private final PropertySource delegate;

    public DelegatingPropertySource(PropertySource delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return delegate.getProperty(name);
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        delegate.forEach(consumer);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public PropertySource subset(String prefix) {
        return delegate.subset(prefix);
    }

    @Override
    public void addListener(Listener listener) {
        delegate.addListener(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        delegate.removeListener(listener);
    }

    protected PropertySource delegate() {
        return delegate;
    }
}
