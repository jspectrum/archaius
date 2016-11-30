package com.netflix.archaius.api;

import java.lang.reflect.Type;
import java.util.Optional;

public interface PropertyConverter {
    <T> Optional<T> convert(String key, Type type);
}
