package com.netflix.archaius.node;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.netflix.archaius.StringConverterRegistry;
import com.netflix.archaius.api.PropertyNode;
import com.netflix.archaius.api.Resolver;
import com.netflix.archaius.api.ResolverLookup;

public class ResolverLookupImpl implements ResolverLookup {
    
    private Map<Type, Resolver<?>> deserializers = new ConcurrentHashMap<>();
    
    public ResolverLookupImpl() {
        StringConverterRegistry.DEFAULT_CONVERTERS.forEach((type, converter) -> {
            deserializers.put(type, new Resolver<Object>() {
                @Override
                public Object resolve(PropertyNode node, ResolverLookup context) {
                    return node.getValue().map(value -> {
                        if (value instanceof String) {
                            return converter.apply((String)value);
                        } else if (value.getClass() == type) {
                            return value;
                        } else {
                            throw new IllegalStateException();
                        }
                    }).orElse(null);
                }
            });
        });
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> Resolver<T> get(Type type) {
        return (Resolver<T>) deserializers.computeIfAbsent(type, t -> {
            if (t instanceof Class) {
                Class<?> cls = (Class<?>)type;
                if (cls.isInterface()) {
                    return new ProxyResolver<>(cls);
                }
                
                throw new IllegalArgumentException("Don't know how to map type " + type.getTypeName());
            }
            
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                if (pType.getRawType() == Map.class) {
                    return new MapResolver(pType.getActualTypeArguments()[0], pType.getActualTypeArguments()[1]);
                } else if (pType.getRawType() == List.class) {
                    return new ListResolver(pType.getActualTypeArguments()[0]);
                } else if (pType.getRawType() == Set.class) {
                    return new SetResolver(pType.getActualTypeArguments()[0]);
                }

                throw new IllegalArgumentException("Don't know how to map type " + type.getTypeName());
            }
            
            throw new IllegalArgumentException("Don't know how to map type " + type.getTypeName());
        });
    }

}
