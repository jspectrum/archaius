package com.netflix.archaius.api;

import java.lang.reflect.Type;
import java.util.Optional;

public interface TypeResolver {
    <T> Optional<T> resolve(Context context, PropertySource source, String key, Type type);
}
