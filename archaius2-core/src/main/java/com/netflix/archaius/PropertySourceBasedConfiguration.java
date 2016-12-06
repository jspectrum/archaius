package com.netflix.archaius;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.netflix.archaius.api.Configuration;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.interpolate.CommonsStrInterpolator;

public class PropertySourceBasedConfiguration implements Configuration {

    static Function<Entry<String, Object>, Entry<String, Supplier<Object>>> interpolate(Function<Object, Object> interpolator) {
        return entry -> new Entry<String, Supplier<Object>>() {
            @Override
            public String getKey() { 
                return entry.getKey(); 
            }
       
            @Override
            public Supplier<Object> getValue() { 
                return () -> interpolator.apply(entry.getValue()); 
            }

            @Override
            public Supplier<Object> setValue(Supplier<Object> value) {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    private final StringConverterRegistry registry;
    private final Function<Object, Object> interpolator;
    private final PropertySource propertySource;
    
    public PropertySourceBasedConfiguration(PropertySource propertySource) {
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

    @Override
    public Stream<Entry<String, Supplier<Object>>> stream() {
        return propertySource.stream().map(interpolate(interpolator));
    }

    @Override
    public Stream<Entry<String, Supplier<Object>>> stream(String prefix) {
        if (!prefix.endsWith(".")) {
            return stream(prefix + ".");
        } else {
            return propertySource.stream(prefix)
                .map(interpolate(interpolator));
        }
    }

    @Override
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

    @Override
    public Optional<Long> getLong(String key) {
        return get(key, Long.class);
    }

    @Override
    public Optional<String> getString(String key) {
        return get(key, String.class);
    }

    @Override
    public Optional<Double> getDouble(String key) {
        return get(key, Double.class);
    }

    @Override
    public Optional<Integer> getInteger(String key) {
        return get(key, Integer.class);
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return get(key, Boolean.class);
    }

    @Override
    public Optional<Short> getShort(String key) {
        return get(key, Short.class);
    }

    @Override
    public Optional<BigInteger> getBigInteger(String key) {
        return get(key, BigInteger.class);
    }

    @Override
    public Optional<BigDecimal> getBigDecimal(String key) {
        return get(key, BigDecimal.class);
    }

    @Override
    public Optional<Float> getFloat(String key) {
        return get(key, Float.class);
    }

    @Override
    public Optional<Byte> getByte(String key) {
        return get(key, Byte.class);
    }

    @Override
    public Optional<?> getProperty(String key) {
        return propertySource.getProperty(key);
    }
    
    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return get(key, type);
    }

    @Override
    public boolean isEmpty() {
        return propertySource.isEmpty();
    }
}
