package com.netflix.archaius.api;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

public interface PropertyResolver {
    <T> Optional<T> getValue(Type type, PropertyNode node, Function<String, String> interpolator);
}
