package com.netflix.archaius.sources;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.SimplePropertyNode;
import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.interpolate.CommonsStrInterpolator;
import com.netflix.archaius.resolvers.PropertyResolverRegistry;

public class ResolvingPropertySource implements PropertySource {

    private final PropertySource delegate;
    private final PropertyResolver resolver;
    private final Function<String, String> interpolator;
    
    public ResolvingPropertySource(PropertySource source) {
        this.delegate = source;
        this.resolver = new PropertyResolverRegistry();
        
        StrInterpolator.Lookup lookup = key -> source.getProperty(key).map(Object::toString).orElse(null);
        this.interpolator = str -> CommonsStrInterpolator.INSTANCE.create(lookup).resolve(str);
    }
    
    public Optional<Long> getLong(String key) {
        return get(Long.class, key);
    }

    public Optional<String> getString(String key) {
        return get(String.class, key);
    }

    public Optional<Double> getDouble(String key) {
        return get(Double.class, key);
    }

    public Optional<Integer> getInteger(String key) {
        return get(Integer.class, key);
    }

    public Optional<Boolean> getBoolean(String key) {
        return get(Boolean.class, key);
    }

    public Optional<Short> getShort(String key) {
        return get(Short.class, key);
    }

    public Optional<BigInteger> getBigInteger(String key) {
        return get(BigInteger.class, key);
    }

    public Optional<BigDecimal> getBigDecimal(String key) {
        return get(BigDecimal.class, key);
    }

    public Optional<Float> getFloat(String key) {
        return get(Float.class, key);
    }

    public Optional<Byte> getByte(String key) {
        return get(Byte.class, key);
    }

    public <T> Optional<T> get(Type type, String key) {
        return resolver.getValue(type, new SimplePropertyNode(delegate, key), interpolator);
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
}
