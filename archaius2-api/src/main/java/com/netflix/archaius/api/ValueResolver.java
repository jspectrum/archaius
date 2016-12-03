package com.netflix.archaius.api;

import java.lang.reflect.Type;
import java.util.Optional;

public interface ValueResolver {
    Optional<Object> resolve(PropertySource source, String key, Type type, ValueResolver resolver);
}
