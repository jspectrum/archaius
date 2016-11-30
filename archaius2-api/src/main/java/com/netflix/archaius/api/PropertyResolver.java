package com.netflix.archaius.api;

import java.lang.reflect.Type;
import java.util.Optional;

public interface PropertyResolver {
    <T> Optional<T> resolve(PropertySource source, String key, Type type, PropertyResolver resolver);
}
