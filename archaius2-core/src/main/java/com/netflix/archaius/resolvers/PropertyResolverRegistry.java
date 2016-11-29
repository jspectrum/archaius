package com.netflix.archaius.resolvers;

import java.lang.reflect.Array;
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
import java.util.Currency;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import javax.xml.bind.DatatypeConverter;

import com.netflix.archaius.api.PropertyNode;
import com.netflix.archaius.api.PropertyResolver;
import com.netflix.archaius.exceptions.ParseException;

public final class PropertyResolverRegistry implements PropertyResolver {
    
    private Map<Class<?>, Function<String, ?>> methods = new ConcurrentHashMap<>();
    
    private Map<Type, Function<String, ?>> stringResolvers;
    
    public PropertyResolverRegistry() {
        stringResolvers = new IdentityHashMap<>(75);
        stringResolvers.put(String.class, v->v);
        stringResolvers.put(boolean.class, v->{
            if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("on")) {
                return Boolean.TRUE;
            }
            else if (v.equalsIgnoreCase("false") || v.equalsIgnoreCase("no") || v.equalsIgnoreCase("off")) {
                return Boolean.FALSE;
            }
            throw new ParseException("Error parsing value '" + v, new Exception("Expected one of [true, yes, on, false, no, off]"));

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

    private Function<String, ?> getStringResolver(Type type) {
        if (stringResolvers.containsKey(type)) {
            return stringResolvers.get(type);
        }
        
        if (type instanceof Class) {
            final Class<?> cls = (Class<?>)type;
            if (cls.isArray()) {
                Function<String, ?> resolver = getStringResolver(cls.getComponentType());
                if (resolver != null) {
                    return encoded -> {
                        String[] elements = encoded.split(",");
                        Object[] ar = (Object[]) Array.newInstance(cls.getComponentType(), elements.length);
                        for (int i = 0; i < elements.length; i++) {
                            final String element = elements[i];
                            ar[i] = resolver.apply(element);
                        }
                        return ar;
                    };
                }
            } else {
                return methods.computeIfAbsent(cls, a -> getStringConverterForType(a));
            }
        }
        
        throw new IllegalArgumentException("Resolver not found for type " + type);
    }
    
    @SuppressWarnings({ "unchecked" })
    @Override
    public <T> Optional<T> getValue(Type type, PropertyNode node, Function<String, String> interpolator) {
        return (Optional<T>) node.getValue().map(value -> {
            try {
                if (value.getClass() == String.class) {
                    return getStringResolver(type).apply(interpolator.apply((String)value));
                } else if (type.equals(value.getClass())) {
                    return value;
                } else {
                    throw new IllegalArgumentException("Bad type " + type);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to get value for " + node, e);
            }
        });
    }

    private static Function<String, ?> getStringConverterForType(Class<?> cls) {
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
