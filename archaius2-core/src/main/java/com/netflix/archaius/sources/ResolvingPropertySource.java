package com.netflix.archaius.sources;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.ValueResolver;
import com.netflix.archaius.resolvers.PropertyResolverBuilder;

public class ResolvingPropertySource extends DelegatingPropertySource implements PropertyResolver {

    private final ValueResolver resolver;
    private final PropertySource delegate;
    
    public ResolvingPropertySource(PropertySource delegate) {
        this(delegate, new PropertyResolverBuilder().build());
    }
    
    public ResolvingPropertySource(PropertySource delegate, ValueResolver resolver) {
        this.delegate = new InterpolatingPropertySource(delegate);
        this.resolver = resolver;
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

    @Override
    public <T> Optional<T> get(String key, Type type) {
        return resolver.resolve(this, key, type, resolver);
    }
    
    @Override
    public PropertySource subset(String prefix) {
        return new ResolvingPropertySource(delegate().subset(prefix)) {
            public PropertySource subset(String childPrefix) {
                return new ResolvingPropertySource(delegate().subset(prefix + "." + childPrefix)); 
            }
        };
    }

    @Override
    protected PropertySource delegate() {
        return delegate;
    }
}
