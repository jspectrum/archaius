package com.netflix.archaius.config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigListener;
import com.netflix.archaius.api.Decoder;
import com.netflix.archaius.api.StrInterpolator;
import com.netflix.archaius.sources.ResolvingPropertySource;

/**
 */
public class PropertySourceConfig implements Config {

    private final ResolvingPropertySource source;
    
    public PropertySourceConfig(ResolvingPropertySource source) {
        this.source = source;
    }
    
    @Override
    public void addListener(ConfigListener listener) {
        // TODO:
    }

    @Override
    public void removeListener(ConfigListener listener) {
        // TODO:
    }

    @Override
    public Object getRawProperty(String key) {
        return source.getProperty(key).orElse(null);
    }

    @Override
    public Long getLong(String key) {
        return get(Long.class, key);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        return get(Long.class, key, defaultValue);
    }

    @Override
    public String getString(String key) {
        return get(String.class, key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return get(String.class, key, defaultValue);
    }

    @Override
    public Double getDouble(String key) {
        return get(Double.class, key);
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        return get(Double.class, key, defaultValue);
    }

    @Override
    public Integer getInteger(String key) {
        return get(Integer.class, key);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        return get(Integer.class, key, defaultValue);
    }

    @Override
    public Boolean getBoolean(String key) {
        return get(Boolean.class, key);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return get(Boolean.class, key, defaultValue);
    }

    @Override
    public Short getShort(String key) {
        return get(Short.class, key);
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        return get(Short.class, key, defaultValue);
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return get(BigInteger.class, key);
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        return get(BigInteger.class, key, defaultValue);
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return get(BigDecimal.class, key);
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        return get(BigDecimal.class, key, defaultValue);
    }

    @Override
    public Float getFloat(String key) {
        return get(Float.class, key);
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        return get(Float.class, key, defaultValue);
    }

    @Override
    public Byte getByte(String key) {
        return get(Byte.class, key);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        return get(Byte.class, key, defaultValue);
    }

    @Override
    public List<?> getList(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<T> getList(String key, Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<?> getList(String key, List<?> defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T get(Class<T> type, String key) {
        return (T) source.getValue(key, type).get();
    }

    @Override
    public <T> T get(Class<T> type, String key, T defaultValue) {
        return (T) source.getValue(key, type).orElse(defaultValue);
    }

    @Override
    public boolean containsKey(String key) {
        return source.getProperty(key).isPresent();
    }

    @Override
    public boolean isEmpty() {
        return source.isEmpty();
    }

    @Override
    public Iterator<String> getKeys() {
        return source.getPropertyNames().iterator();
    }

    @Override
    public Iterator<String> getKeys(String prefix) {
        return new TreeSet<>(source.getPropertyNames()).subSet(prefix, prefix + "/uffff").iterator();
    }

    @Override
    public Config getPrefixedView(String prefix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStrInterpolator(StrInterpolator interpolator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StrInterpolator getStrInterpolator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDecoder(Decoder decoder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Decoder getDecoder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        throw new UnsupportedOperationException();
    }
}
