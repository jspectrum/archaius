package com.netflix.archaius.sources;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.api.ValueResolver;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.resolvers.PropertyResolverBuilder;

public class ResolvingPropertySource extends DelegatingPropertySource implements PropertyResolver {

    private final ValueResolver resolver;
    
    public ResolvingPropertySource(PropertySource delegate) {
        this(delegate, new PropertyResolverBuilder().build());
    }
    
    public ResolvingPropertySource(PropertySource delegate, ValueResolver resolver) {
        super(delegate);
        this.resolver = resolver;
    }
    
    public Optional<Long> getLong(String key) {
        return resolve(key, Long.class);
    }

    public Optional<String> getString(String key) {
        return resolve(key, String.class);
    }

    public Optional<Double> getDouble(String key) {
        return resolve(key, Double.class);
    }

    public Optional<Integer> getInteger(String key) {
        return resolve(key, Integer.class);
    }

    public Optional<Boolean> getBoolean(String key) {
        return resolve(key, Boolean.class);
    }

    public Optional<Short> getShort(String key) {
        return resolve(key, Short.class);
    }

    public Optional<BigInteger> getBigInteger(String key) {
        return resolve(key, BigInteger.class);
    }

    public Optional<BigDecimal> getBigDecimal(String key) {
        return resolve(key, BigDecimal.class);
    }

    public Optional<Float> getFloat(String key) {
        return resolve(key, Float.class);
    }

    public Optional<Byte> getByte(String key) {
        return resolve(key, Byte.class);
    }

    @Override
    public <T> Optional<T> resolve(String key, Type type) {
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
}
