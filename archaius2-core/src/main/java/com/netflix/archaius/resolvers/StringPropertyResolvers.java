package com.netflix.archaius.resolvers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.BitSet;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import javax.xml.bind.DatatypeConverter;

final class StringPropertyResolvers {
    private StringPropertyResolvers() {}
    
    private static final Map<Type, Function<String, ?>> stringResolvers = new IdentityHashMap<>(75);
    
    static {
        stringResolvers.put(String.class, v->v);
        stringResolvers.put(boolean.class, v->{
            if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("on")) {
                return Boolean.TRUE;
            }
            else if (v.equalsIgnoreCase("false") || v.equalsIgnoreCase("no") || v.equalsIgnoreCase("off")) {
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Error parsing value '" + v, new Exception("Expected one of [true, yes, on, false, no, off]"));
        });
        stringResolvers.put(Boolean.class, stringResolvers.get(boolean.class));
        stringResolvers.put(Integer.class, Integer::valueOf);
        stringResolvers.put(int.class, Integer::valueOf);
        stringResolvers.put(long.class, Long::valueOf);
        stringResolvers.put(Long.class, Long::valueOf);
        stringResolvers.put(short.class, Short::valueOf);
        stringResolvers.put(Short.class, Short::valueOf);
        stringResolvers.put(byte.class, Byte::valueOf);
        stringResolvers.put(Byte.class, Byte::valueOf);
        stringResolvers.put(double.class, Double::valueOf);
        stringResolvers.put(Double.class, Double::valueOf);
        stringResolvers.put(float.class, Float::valueOf);
        stringResolvers.put(Float.class, Float::valueOf);
        stringResolvers.put(BigInteger.class, BigInteger::new);
        stringResolvers.put(BigDecimal.class, BigDecimal::new);
        stringResolvers.put(AtomicInteger.class, s->new AtomicInteger(Integer.parseInt(s)));
        stringResolvers.put(AtomicLong.class, s->new AtomicLong(Long.parseLong(s)));
        stringResolvers.put(Duration.class, Duration::parse);
        stringResolvers.put(Period.class, Period::parse);
        stringResolvers.put(LocalDateTime.class, LocalDateTime::parse);
        stringResolvers.put(LocalDate.class, LocalDate::parse);
        stringResolvers.put(LocalTime.class, LocalTime::parse);
        stringResolvers.put(OffsetDateTime.class, OffsetDateTime::parse);
        stringResolvers.put(OffsetTime.class, OffsetTime::parse);
        stringResolvers.put(ZonedDateTime.class, ZonedDateTime::parse);
        stringResolvers.put(Instant.class, v->Instant.from(OffsetDateTime.parse(v)));
        stringResolvers.put(Date.class, v->new Date(Long.parseLong(v)));
        stringResolvers.put(Currency.class, Currency::getInstance);
        stringResolvers.put(BitSet.class, v->BitSet.valueOf(DatatypeConverter.parseHexBinary(v)));
    }
    
    static final Map<Type, Function<String, ?>> getDefaultStringResolvers() {
        return Collections.unmodifiableMap(stringResolvers);
    }
    
    static Function<String, ?> forClass(Class<?> cls) {
        // Next look a valueOf(String) static method
        try {
            Method method;
            try {
                method = cls.getMethod("valueOf", String.class);
                return encoded -> {
                    try {
                        return method.invoke(null, encoded);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Unable to convert '" + encoded + "' to " + cls.getName(), e);
                    }
                };
            } catch (NoSuchMethodException e1) {
                // Next look for a T(String) constructor
                Constructor<?> c;
                try {
                    c = (Constructor<?>) cls.getConstructor(String.class);
                    return encoded -> { 
                        try {
                            return c.newInstance(encoded);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Unable to convert '" + encoded + "' to " + cls.getName(), e);
                        }
                    };
                }
                catch (NoSuchMethodException e) {
                    throw new RuntimeException(cls.getCanonicalName() + " has no String constructor or valueOf static method");
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to instantiate value of type " + cls.getCanonicalName(), e);
        }
    }
}
