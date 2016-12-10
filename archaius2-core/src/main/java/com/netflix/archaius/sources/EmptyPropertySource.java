package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
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
    public Collection<String> getKeys() {
        return Collections.emptySet();
    }

    @Override
    public Collection<String> getKeys(String prefix) {
        return Collections.emptySet();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }
    
    @Override
    public AutoCloseable addListener(Consumer<PropertySource> consumer) {
        return () -> {};
    }

//    @Override
//    public Stream<Entry<String, Object>> stream() {
//        return Stream.empty();
//    }
//
//    @Override
//    public Stream<Entry<String, Object>> stream(String prefix) {
//        return Stream.empty();
//    }

    @Override
    public PropertySource snapshot() {
        return this;
    }
}
