package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.netflix.archaius.api.Cancellation;
import com.netflix.archaius.api.PropertySource;

public abstract class DelegatingPropertySource implements PropertySource {

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return delegate().getProperty(name);
    }

    @Override
    public void forEach(BiConsumer<String, Supplier<Object>> consumer) {
        delegate().forEach(consumer);
    }

    @Override
    public void forEach(String prefix, BiConsumer<String, Supplier<Object>> consumer) {
        delegate().forEach(prefix, consumer);
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
    public Cancellation addListener(Consumer<PropertySource> consumer) {
        return delegate().addListener(consumer);
    }

    protected abstract PropertySource delegate();
}
