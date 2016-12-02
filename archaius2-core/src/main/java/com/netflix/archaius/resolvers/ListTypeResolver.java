package com.netflix.archaius.resolvers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import com.netflix.archaius.api.Context;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.TypeResolver;

public class ListTypeResolver implements TypeResolver {
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolve(Context context, PropertySource source, String key, Type type) {
        ParameterizedType pType = (ParameterizedType)type;
        Type valueType = pType.getActualTypeArguments()[0];
        return (Optional<T>) source
            .getProperty(key)
            .map(value -> {
                if (value.getClass() == String.class) {
                    return Arrays
                        .asList(((String)value).split(","))
                        .stream()
                        .map(v -> (T)context.resolve(v, valueType))
                        .collect(Collectors.toList());
                }
                throw new IllegalArgumentException(key + " expected to be string");
            });
    }
}
