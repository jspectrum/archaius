package com.netflix.archaius.resolvers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.netflix.archaius.api.Context;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.TypeResolver;

public class SetTypeResolver implements TypeResolver {
    private Supplier<Set> supplier;

    public SetTypeResolver() {
        this(HashSet::new);
    }
    
    public SetTypeResolver(Supplier<Set> supplier) {
        this.supplier = supplier;
    }

    @Override
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
                        .collect(Collectors.toCollection(supplier));
                }
                throw new IllegalArgumentException(key + " expected to be string");
            });
        }
}
