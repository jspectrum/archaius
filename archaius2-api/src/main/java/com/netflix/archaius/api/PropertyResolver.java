package com.netflix.archaius.api;

import java.lang.reflect.Type;
import java.util.Optional;

public interface PropertyResolver {
    <T> Optional<T> get(String key, Type type);
    
    default <T> Optional<T> get(String key, Class<T> type) {
        return get(key, (Type)type);
    }
}
