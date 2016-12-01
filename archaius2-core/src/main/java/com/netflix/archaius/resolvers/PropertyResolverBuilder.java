package com.netflix.archaius.resolvers;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.ValueResolver;

public class PropertyResolverBuilder {
    private final Map<Type, Function<String, ?>> dynamic = new ConcurrentHashMap<>();
    private final Map<Type, Function<String, ?>> known;
    private final ValueResolver interfaceResolver = new InterfacePropertyResolver();
    private final Map<Type, ValueResolver> parameterizedResolvers = new HashMap<>();
    
    public PropertyResolverBuilder() {
        known = new IdentityHashMap<>(75);
        known.putAll(StringPropertyResolvers.getDefaultStringResolvers());
        parameterizedResolvers.put(Map.class, new MapPropertyResolver());
        parameterizedResolvers.put(Set.class, new SetPropertyResolver());
        parameterizedResolvers.put(List.class, new ListPropertyResolver());
        parameterizedResolvers.put(SortedSet.class, new SortedSetPropertyResolver());
        parameterizedResolvers.put(SortedMap.class, new SortedMapPropertyResolver());
    }

    private boolean isInterface(Type type) {
        if (type instanceof Class) {
            return ((Class<?>)type).isInterface();
        }
        return false;
    }
    
    public ValueResolver build() {
        return new ValueResolver() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> Optional<T> resolve(PropertySource source, String key, Type type, ValueResolver resolver) {
                if (type instanceof ParameterizedType) {
                    ValueResolver valueResolver = parameterizedResolvers.get(((ParameterizedType) type).getRawType());
                    if (valueResolver != null) {
                        return valueResolver.resolve(source, key, type, resolver);
                    } else {
                        return Optional.empty();
                    }
                } 
                Optional<T> result = source.getProperty(key).map(value -> {
                    if (value.getClass() == String.class) {
                        return (T) getStringResolver(type).apply((String)value);
                    } else if (type.equals(value.getClass())) {
                        return (T) value;
                    } else {
                        throw new IllegalArgumentException("Unexpected type " + value.getClass() + " for '" + key + "'. Expecting String or " + type.getTypeName());
                    }
                });
                
                if (!result.isPresent() && isInterface(type)) {
                    return interfaceResolver.resolve(source, key, type, resolver);
                }
                return result;
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
