package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.netflix.archaius.api.Cancellation;
import com.netflix.archaius.api.PropertySource;

public class SinglePropertySource implements PropertySource {
    private final String key;
    private final Optional<Object> value;
    
    public SinglePropertySource(String key, Object value) {
        this.key = key;
        this.value = Optional.of(value);
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Optional<Object> getProperty(String key) {
        if (this.key.equals(key)) {
            return value;
        }
        return Optional.empty();
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        consumer.accept(key, value.get());
    }

    @Override
    public Collection<String> getPropertyNames() {
        return Collections.singleton(key);
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
    public Cancellation addListener(Listener listener) {
        return Cancellation.empty();
    }
}
