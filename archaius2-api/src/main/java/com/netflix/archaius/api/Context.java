package com.netflix.archaius.api;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * TODO: This needs a better name 
 */
public interface Context {

    <T> T resolve(String value, Type type);

    <T> Optional<T> resolve(PropertySource source, String key, Type type);
}
