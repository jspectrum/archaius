package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.netflix.archaius.api.PropertySource;

public class EmptyPropertySource implements PropertySource {

    public static EmptyPropertySource INSTANCE = new EmptyPropertySource();
    
    private EmptyPropertySource() {
        
    }
    
    @Override
    public String getName() {
        return "empty";
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return Optional.empty();
    }

    @Override
    public Collection<String> getPropertyNames() {
        return Collections.emptySet();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Runnable addListener(Consumer<PropertySource> consumer) {
        return () -> {};
    }

    @Override
    public Stream<Entry<String, Supplier<Object>>> stream() {
        return Stream.empty();
    }

    @Override
    public Stream<Entry<String, Supplier<Object>>> stream(String prefix) {
        return Stream.empty();
    }
}
