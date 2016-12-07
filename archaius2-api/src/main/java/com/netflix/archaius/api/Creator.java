package com.netflix.archaius.api;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 *
 * @param <T>
 */
public interface Creator<T> {
    void onProperty(String key, Supplier<Object> value);
    T create();
    
    default Collector<Map.Entry<String, Supplier<Object>>, ? extends Creator<T>, T> toCollector() {
        return Collector.of(
                () -> this,
                (creator, entry) -> creator.onProperty(entry.getKey(), entry.getValue()),
                (Creator<T> c1, Creator<T> c2) -> c1, 
                (accum) -> accum.create());
    }
}

