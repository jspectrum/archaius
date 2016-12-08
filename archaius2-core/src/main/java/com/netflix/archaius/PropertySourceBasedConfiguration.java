package com.netflix.archaius;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import com.netflix.archaius.api.Configuration;
import com.netflix.archaius.api.PropertyNode;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.ResolverLookup;
import com.netflix.archaius.node.PropertySourcePropertyNode;
import com.netflix.archaius.node.ResolverLookupImpl;
import com.netflix.archaius.sources.InterpolatingPropertySource;

public class PropertySourceBasedConfiguration<PS extends PropertySource> implements Configuration<PS> {

    private final ResolverLookup lookup;
    private final PS source;
    private final PropertySource interpolated;
    
    public PropertySourceBasedConfiguration(PS source) {
        this.interpolated = new InterpolatingPropertySource(source);
        this.source = source;
        this.lookup = new ResolverLookupImpl();
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
        return source.getProperty(key);
    }
    
    @Override
    public Optional<Object> get(String key, Type type) {
        PropertyNode node = new PropertySourcePropertyNode(interpolated, key);
        return Optional.ofNullable(lookup.get(type).resolve(node, lookup));
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        PropertyNode node = new PropertySourcePropertyNode(interpolated, key);
        return Optional.ofNullable(lookup.get(type).resolve(node, lookup));
    }

    @Override
    public boolean isEmpty() {
        return source.isEmpty();
    }

    @Override
    public PS getPropertySource() {
        return source;
    }
}
