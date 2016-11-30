package com.netflix.archaius.sources;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.api.PropertySource;

public class PropertyResolverBuilder {
    private final Map<Type, Function<String, ?>> dynamic = new ConcurrentHashMap<>();
    private final Map<Type, Function<String, ?>> known;
    
    public PropertyResolverBuilder() {
        known = new IdentityHashMap<>(75);
        known.putAll(StringPropertyResolvers.getDefaultStringResolvers());
    }

    public PropertyResolver build() {
        return new PropertyResolver() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> Optional<T> resolve(PropertySource source, String key, Type type, PropertyResolver resolver) {
                return source.getProperty(key).map(value -> {
                    if (value.getClass() == String.class) {
                        return (T) getStringResolver(type).apply((String)value);
                    } else if (type.equals(value.getClass())) {
                        return (T) value;
                    } else {
                        throw new IllegalArgumentException("Unexpected type " + value.getClass() + " for '" + key + "'. Expecting String or " + type.getTypeName());
                    }
                });
            }
            
            private Function<String, ?> getStringResolver(Type type) {
                if (known.containsKey(type)) {
                    return known.get(type);
                }
                
                if (type instanceof Class) {
                    final Class<?> cls = (Class<?>)type;
                    
                    return dynamic.computeIfAbsent(type, t -> {
                        if (cls.isArray()) {
                            final Function<String, ?> componentResolver = getStringResolver(cls.getComponentType());
                            return encoded -> {
                                String[] components = encoded.split(",");
                                Object ar = Array.newInstance(cls.getComponentType(), components.length);
                                for (int i = 0; i < components.length; i++) {
                                    Array.set(ar, i, componentResolver.apply(components[i]));
                                }
                                return ar;
                            };
                        } else {
                            return StringPropertyResolvers.forClass(cls);
                        }
                    });
                }
                
                throw new IllegalArgumentException("Resolver not found for type " + type);
            }
        };
    }
}
