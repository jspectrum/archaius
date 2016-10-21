package com.netflix.archaius;

import com.netflix.archaius.api.ConfigNode;
import com.netflix.archaius.api.TypeDecoder;
import com.netflix.archaius.api.TypeDecoders;
import com.netflix.archaius.api.TypeMatcher;
import com.netflix.archaius.api.TypeToken;
import com.netflix.archaius.exceptions.ParseException;

import java.lang.reflect.Array;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.xml.bind.DatatypeConverter;

public class DefaultTypeDecoders implements TypeDecoders {
    private static final Function<String, Boolean> BOOLEAN_DECODER = v->{
            if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("on")) {
                return Boolean.TRUE;
            }
            else if (v.equalsIgnoreCase("false") || v.equalsIgnoreCase("no") || v.equalsIgnoreCase("off")) {
                return Boolean.FALSE;
            }
            throw new ParseException("Error parsing value '" + v, new Exception("Expected one of [true, yes, on, false, no, off]"));
    
        };
    
    private static UnaryOperator<Builder> defaultTypesOperator() {
        return builder -> builder
            .withStringDecoder(String.class, v -> v)
            .withStringDecoder(boolean.class, BOOLEAN_DECODER)
            .withStringDecoder(Boolean.class, BOOLEAN_DECODER)
            .withStringDecoder(Integer.class, Integer::valueOf)
            .withStringDecoder(int.class, Integer::valueOf)
            .withStringDecoder(long.class, Long::valueOf)
            .withStringDecoder(Long.class, Long::valueOf)
            .withStringDecoder(short.class, Short::valueOf)
            .withStringDecoder(Short.class, Short::valueOf)
            .withStringDecoder(byte.class, Byte::valueOf)
            .withStringDecoder(Byte.class, Byte::valueOf)
            .withStringDecoder(double.class, Double::valueOf)
            .withStringDecoder(Double.class, Double::valueOf)
            .withStringDecoder(float.class, Float::valueOf)
            .withStringDecoder(Float.class, Float::valueOf)
            .withStringDecoder(BigInteger.class, BigInteger::new)
            .withStringDecoder(BigDecimal.class, BigDecimal::new)
            .withStringDecoder(AtomicInteger.class, s->new AtomicInteger(Integer.parseInt(s)))
            .withStringDecoder(AtomicLong.class, s->new AtomicLong(Long.parseLong(s)))
            .withStringDecoder(Duration.class, Duration::parse)
            .withStringDecoder(Period.class, Period::parse)
            .withStringDecoder(LocalDateTime.class, LocalDateTime::parse)
            .withStringDecoder(LocalDate.class, LocalDate::parse)
            .withStringDecoder(LocalTime.class, LocalTime::parse)
            .withStringDecoder(OffsetDateTime.class, OffsetDateTime::parse)
            .withStringDecoder(OffsetTime.class, OffsetTime::parse)
            .withStringDecoder(ZonedDateTime.class, ZonedDateTime::parse)
            .withStringDecoder(Instant.class, v->Instant.from(OffsetDateTime.parse(v)))
            .withStringDecoder(Date.class, v->new Date(Long.parseLong(v)))
            .withStringDecoder(Currency.class, Currency::getInstance)
            .withStringDecoder(BitSet.class, v->BitSet.valueOf(DatatypeConverter.parseHexBinary(v)))
            .<Object>withTypeDecoder(
                type -> type.getRawType().isArray(),
                (type, node, decoders) -> {
                    Object value = node.value();
                    assert (value instanceof String);
                    String str = (String)value;
                    String[] elements = str.split(",");
                    
                    Class<?> componentType = type.getRawType().getComponentType();
                    Object[] ar = (Object[]) Array.newInstance(componentType, elements.length);
                    for (int i = 0; i < elements.length; i++) {
                        ar[i] = decoders.decode(componentType, ScalarNode.from(elements[i], node));
                    }
                    return ar;
                });
    }
    
    public static class Builder {
        DefaultTypeDecoders instance = new DefaultTypeDecoders();
        
        public <T> Builder withStringDecoder(final Class<T> classType, Function<String, T> decoder) {
            instance.typeDecoders.put(classType, new TypeDecoder<T>() {
                @SuppressWarnings("unchecked")
                @Override
                public T decode(TypeToken type, ConfigNode node, TypeDecoders decoders) {
                    Object value = node.value();
                    if (value.getClass().isAssignableFrom(classType)) {
                        return (T)value;
                    } else if (value instanceof String) {
                        return decoder.apply((String)value);
                    } else {
                        throw new IllegalArgumentException("Node must be a String value but was " + value.getClass());
                    }
                }
            });
            return this;
        }
        
        public <T> Builder withStringDecoder(final TypeMatcher matcher, BiFunction<TypeToken, String, T> decoder) {
            instance.matchingDecoders.put(matcher, new TypeDecoder<T>() {
                @Override
                public T decode(TypeToken type, ConfigNode node, TypeDecoders decoders) {
                    Object value = node.value();
                    if (value instanceof String) {
                        return decoder.apply(type, (String)value);
                    } else {
                        throw new IllegalArgumentException("Node must be a String value but was " + value.getClass());
                    }
                }
            });
            return this;
        }
        
        public <T> Builder withTypeDecoder(Type type, TypeDecoder<T> decoder) {
            instance.typeDecoders.put(type, decoder);
            return this;
        }
        
        public <T> Builder withTypeDecoder(TypeMatcher matcher, TypeDecoder<T> decoder) {
            instance.matchingDecoders.put(matcher, decoder);
            return this;
        }
        
        public DefaultTypeDecoders build() {
            try {
                return instance;
            } finally {
                instance = null;
            }
        }
    }
    
    public static Builder builder() {
        return defaultTypesOperator().apply(new Builder());
    }
    
    private final Map<Type, TypeDecoder<?>> typeDecoders = new HashMap<>();;
    private final Map<TypeMatcher, TypeDecoder<?>> matchingDecoders = new LinkedHashMap<>();;
    
    private DefaultTypeDecoders() {
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeDecoder<T> getTypeDecoder(TypeToken type) {
        TypeDecoder<?> decoder = typeDecoders.get(type.getRawType());
        if (decoder != null) {
            return (TypeDecoder<T>) decoder;
        }
            
        for (Entry<TypeMatcher, TypeDecoder<?>> element : matchingDecoders.entrySet()) {
            if (element.getKey().matches(type)) {
                return (TypeDecoder<T>) element.getValue();
            }
        }
        return notFound(type);
    }
    
    @Override
    public <T> T decode(TypeToken type, ConfigNode node) {
        TypeDecoder<T> decoder = getTypeDecoder(type);
        if (decoder != null) {
            return decoder.decode(type, node, this);
        }
        return notFound(type);
    }
    
    private <T> T notFound(TypeToken type) {
        return null;
    }
}
