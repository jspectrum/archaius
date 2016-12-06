package com.netflix.archaius.creator;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.netflix.archaius.StringConverterRegistry;
import com.netflix.archaius.api.Creator;
import com.netflix.archaius.api.CreatorFactory;
import com.netflix.archaius.api.Matcher;

public class CreatorFactoryBuilder {

    private StringConverterRegistry registry;
    private List<MatchingCreator> custom = new ArrayList<>();
    
    private static interface MatchingCreator {
        boolean matches(Type type);
        Creator<?> get();
    }
    
    public CreatorFactoryBuilder() {
        this.registry = StringConverterRegistry.newBuilder().build();
    }
    
    CreatorFactoryBuilder register(Matcher matcher, Creator<?> creator) {
        custom.add(new MatchingCreator() {
            @Override
            public boolean matches(Type type) {
                return matcher.matches(type);
            }

            @Override
            public Creator<?> get() {
                return creator;
            }
        });
        return this;
    }
    
    public CreatorFactory build() {
        return new CreatorFactory() {
            @Override
            public Creator<?> create(Type type, Annotation[] annotations) {
                Function<String, ?> converter = registry.getConverter(type);
                if (converter != null) {
                    return new SimpleTypeCreator(converter, annotations);
                }
                
                for (MatchingCreator creator : custom) {
                    if (creator.matches(type)) {
                        return creator.get();
                    }
                }
                
                if (type instanceof Class) {
                    Class<?> cls = (Class<?>)type;
                    if (cls.isInterface()) {
                        return new ProxyTypeCreator<>(this, cls, annotations);
                    }
                    
                    throw new IllegalArgumentException("Don't know how to map type " + type.getTypeName());
                }
                
                if (type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) type;
                    if (pType.getRawType() == Map.class) {
                        return new MapTypeCreator(LinkedHashMap::new, () -> create(pType.getActualTypeArguments()[1], null));
                    } else if (pType.getRawType() == List.class) {
                        return new ListTypeCreator(registry.getConverter(pType.getActualTypeArguments()[0]), annotations);
                    } else if (pType.getRawType() == Set.class) {
                        return new SetTypeCreator(registry.getConverter(pType.getActualTypeArguments()[0]), annotations);
                    }
                }
                
                throw new IllegalArgumentException("Don't know how to map type " + type);
            }
        };
    }
}
