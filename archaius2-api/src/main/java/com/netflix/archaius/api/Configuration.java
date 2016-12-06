package com.netflix.archaius.api;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Configuration {

    public Optional<Long> getLong(String key);

    public Optional<String> getString(String key);

    public Optional<Double> getDouble(String key);

    public Optional<Integer> getInteger(String key);

    public Optional<Boolean> getBoolean(String key);

    public Optional<Short> getShort(String key);

    public Optional<BigInteger> getBigInteger(String key);

    public Optional<BigDecimal> getBigDecimal(String key);

    public Optional<Float> getFloat(String key);

    public Optional<Byte> getByte(String key);

    public Optional<?> getProperty(String key);
    
    public Optional<Object> get(String key, Type type);
    
    public <T> Optional<T> get(String key, Class<T> type);

    public Stream<Entry<String, Supplier<Object>>> stream();

    public Stream<Entry<String, Supplier<Object>>> stream(String prefix);

    public boolean isEmpty();
}
