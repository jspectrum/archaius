package com.netflix.archaius.sources;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import com.netflix.archaius.api.Context;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.resolvers.ContextImpl;

public class ResolvingPropertySource extends DelegatingPropertySource {

    private final Context context;
    private final PropertySource delegate;
    
    public ResolvingPropertySource(PropertySource delegate) {
        this(delegate, new ContextImpl());
    }
    
    public ResolvingPropertySource(PropertySource delegate, Context context) {
        this.delegate = new InterpolatingPropertySource(delegate);
        this.context = context;
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

    public <T> Optional<T> get(String key, Type type) {
        return context.resolve(this, key, type);
    }
    
    public <T> Optional<T> get(String key, Class<T> type) {
        return context.resolve(this, key, type);
    }
    
    @Override
    protected PropertySource delegate() {
        return delegate;
    }
}
