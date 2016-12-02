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

import com.netflix.archaius.api.Context;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.TypeResolver;

public class ContextImpl implements Context {
    private final Map<Type, Function<String, ?>> dynamic = new ConcurrentHashMap<>();
    private final Map<Type, Function<String, ?>> known;
    private final TypeResolver interfaceResolver = new InterfaceTypeResolver();
    private final Map<Type, TypeResolver> parameterizedResolvers = new HashMap<>();
    
    public ContextImpl() {
        known = new IdentityHashMap<>(75);
        known.putAll(StringPropertyResolvers.getDefaultStringResolvers());
        parameterizedResolvers.put(Map.class, new MapTypeResolver());
        parameterizedResolvers.put(Set.class, new SetTypeResolver());
        parameterizedResolvers.put(List.class, new ListTypeResolver());
        parameterizedResolvers.put(SortedSet.class, new SortedSetTypeResolver());
        parameterizedResolvers.put(SortedMap.class, new SortedMapTypeResolver());
    }

    private boolean isInterface(Type type) {
        if (type instanceof Class) {
            return ((Class<?>)type).isInterface();
        }
        return false;
    }
    
    @Override
    public <T> T resolve(String value, Type type) {
        return (T) getStringResolver(type).apply(value);
    }
    
    @Override
    public <T> Optional<T> resolve(PropertySource source, String key, Type type) {
        if (type instanceof ParameterizedType) {
            TypeResolver valueResolver = parameterizedResolvers.get(((ParameterizedType) type).getRawType());
            if (valueResolver != null) {
                return valueResolver.resolve(this, source, key, type);
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
            return interfaceResolver.resolve(this, source, key, type);
        }
        return result;
    }
    
    public TypeResolver getResolver(Type type) {
        return null;
    }
    
    public Function<String, ?> getStringResolver(Type type) {
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
}
