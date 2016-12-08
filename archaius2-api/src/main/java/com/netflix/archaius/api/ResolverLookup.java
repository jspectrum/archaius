package com.netflix.archaius.api;

import java.lang.reflect.Type;

/**
 * The ResolverLookup contains the logic to determine the Resolver for a type.
 */
public interface ResolverLookup {

    <T> Resolver<T> get(Type type);

    default <T> Resolver<T> get(Class<T> type) {
        return get((Type)type);
    }
    
}
