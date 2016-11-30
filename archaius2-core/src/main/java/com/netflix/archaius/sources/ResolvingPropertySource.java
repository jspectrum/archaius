package com.netflix.archaius.sources;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.interpolate.CommonsStrInterpolator;

public class ResolvingPropertySource implements PropertySource, PropertyResolver {

    private final PropertySource delegate;
    private final Function<String, String> interpolator;
    private final Map<Type, Function<String, ?>> dynamic = new ConcurrentHashMap<>();
    private final Map<Type, Function<String, ?>> known;

    public ResolvingPropertySource(PropertySource delegate) {
        this.delegate = delegate;
        
        StrInterpolator.Lookup lookup = key -> delegate.getProperty(key).map(Object::toString).orElse(null);
        this.interpolator = str -> CommonsStrInterpolator.INSTANCE.create(lookup).resolve(str);
        
        known = new IdentityHashMap<>(75);
        known.putAll(StringPropertyResolvers.getDefaultStringResolvers());
    }
    
    public Optional<Long> getLong(String key) {
        return getValue(key, Long.class);
    }

    public Optional<String> getString(String key) {
        return getValue(key, String.class);
    }

    public Optional<Double> getDouble(String key) {
        return getValue(key, Double.class);
    }

    public Optional<Integer> getInteger(String key) {
        return getValue(key, Integer.class);
    }

    public Optional<Boolean> getBoolean(String key) {
        return getValue(key, Boolean.class);
    }

    public Optional<Short> getShort(String key) {
        return getValue(key, Short.class);
    }

    public Optional<BigInteger> getBigInteger(String key) {
        return getValue(key, BigInteger.class);
    }

    public Optional<BigDecimal> getBigDecimal(String key) {
        return getValue(key, BigDecimal.class);
    }

    public Optional<Float> getFloat(String key) {
        return getValue(key, Float.class);
    }

    public Optional<Byte> getByte(String key) {
        return getValue(key, Byte.class);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return delegate.getProperty(name);
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        delegate.forEach(consumer);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public <T> Optional<T> getValue(String key, Type type) {
        return delegate.getProperty(key).map(value -> {
            if (value.getClass() == String.class) {
                return (T) getStringResolver(type).apply(interpolator.apply((String)value));
            } else if (type.equals(value.getClass())) {
                return (T) value;
            } else {
                throw new IllegalArgumentException("Unexpected type " + value.getClass() + " for '" + key + "'. Expecting String or " + type.getTypeName());
            }
        });
    }
    
    public <T> Optional<T> getValue(String key, Class<T> type) {
        return getValue(key, type);
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

    @Override
    public PropertySource subset(String prefix) {
        return new ResolvingPropertySource(delegate.subset(prefix)) {
            public PropertySource subset(String childPrefix) {
                return new ResolvingPropertySource(delegate.subset(prefix + "." + childPrefix)); 
            }
        };
    }
}
