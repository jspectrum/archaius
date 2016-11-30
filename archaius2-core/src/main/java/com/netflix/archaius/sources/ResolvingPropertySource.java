package com.netflix.archaius.sources;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import com.netflix.archaius.api.PropertyConverter;
import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.api.PropertySource;

public class ResolvingPropertySource extends DelegatingPropertySource implements PropertyConverter {

    private final PropertyResolver resolver;
    
    public ResolvingPropertySource(PropertySource delegate) {
        this(delegate, new PropertyResolverBuilder().build());
    }
    
    public ResolvingPropertySource(PropertySource delegate, PropertyResolver resolver) {
        super(delegate);
        this.resolver = resolver;
    }
    
    public Optional<Long> getLong(String key) {
        return convert(key, Long.class);
    }

    public Optional<String> getString(String key) {
        return convert(key, String.class);
    }

    public Optional<Double> getDouble(String key) {
        return convert(key, Double.class);
    }

    public Optional<Integer> getInteger(String key) {
        return convert(key, Integer.class);
    }

    public Optional<Boolean> getBoolean(String key) {
        return convert(key, Boolean.class);
    }

    public Optional<Short> getShort(String key) {
        return convert(key, Short.class);
    }

    public Optional<BigInteger> getBigInteger(String key) {
        return convert(key, BigInteger.class);
    }

    public Optional<BigDecimal> getBigDecimal(String key) {
        return convert(key, BigDecimal.class);
    }

    public Optional<Float> getFloat(String key) {
        return convert(key, Float.class);
    }

    public Optional<Byte> getByte(String key) {
        return convert(key, Byte.class);
    }

    @Override
    public <T> Optional<T> convert(String key, Type type) {
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
