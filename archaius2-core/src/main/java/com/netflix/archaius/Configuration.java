package com.netflix.archaius;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.interpolate.CommonsStrInterpolator;
import com.netflix.archaius.resolvers.StringConverterRegistry;
import com.netflix.archaius.sources.PropertySourceUtils;

public class Configuration {

    private final StringConverterRegistry registry;
    private final Function<Object, Object> interpolator;
    private final PropertySource propertySource;
    
    public Configuration(PropertySource propertySource) {
        this.propertySource = propertySource;
        
        StrInterpolator.Lookup lookup = key -> propertySource.getProperty(key).map(Object::toString).orElse(null);
        this.interpolator = value -> {
            if (value.getClass() == String.class) {
                return CommonsStrInterpolator.INSTANCE.create(lookup).resolve((String)value);
            }
            return value;
        };
        
        this.registry = StringConverterRegistry.newBuilder().build();
    }

    public Stream<Entry<String, Supplier<Object>>> stream() {
        return propertySource.stream().map(PropertySourceUtils.interpolate(interpolator));
    }

    public Stream<Entry<String, Supplier<Object>>> stream(String prefix) {
        if (!prefix.endsWith(".")) {
            return stream(prefix + ".");
        } else {
            return propertySource.stream(prefix)
                .map(PropertySourceUtils.interpolate(interpolator));
        }
    }

    public Optional<Object> get(String key, Type type) {
        return propertySource.getProperty(key).map(value -> {
            if (value.getClass() == String.class) {
                return registry.getConverter(type).apply((String)value);
            } else if (value.getClass() == type) {
                return value;
            } else {
                throw new IllegalArgumentException("Expected String or " + value.getClass() + " but got " + type.getTypeName());
            }
        });
    }

    public Optional<Long> getLong(String key) {
        return get(key, Long.class);
    }

    public Optional<String> getString(String key) {
        return get(key, String.class);
    }

    public Optional<Double> getDouble(String key) {
        return get(key, Double.class);
    }

    public Optional<Integer> getInteger(String key) {
        return get(key, Integer.class);
    }

    public Optional<Boolean> getBoolean(String key) {
        return get(key, Boolean.class);
    }

    public Optional<Short> getShort(String key) {
        return get(key, Short.class);
    }

    public Optional<BigInteger> getBigInteger(String key) {
        return get(key, BigInteger.class);
    }

    public Optional<BigDecimal> getBigDecimal(String key) {
        return get(key, BigDecimal.class);
    }

    public Optional<Float> getFloat(String key) {
        return get(key, Float.class);
    }

    public Optional<Byte> getByte(String key) {
        return get(key, Byte.class);
    }

    public Optional<?> getProperty(String key) {
        return propertySource.getProperty(key);
    }
    
    public <T> Optional<T> get(String key, Class<T> type) {
        return get(key, type);
    }

    public boolean isEmpty() {
        return propertySource.isEmpty();
    }
}
