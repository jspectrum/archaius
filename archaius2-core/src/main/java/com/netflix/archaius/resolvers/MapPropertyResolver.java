package com.netflix.archaius.resolvers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.ValueResolver;

public class MapPropertyResolver implements ValueResolver {
    private final Supplier<Map> mapSupplier;

    MapPropertyResolver(Supplier<Map> mapSupplier) {
        this.mapSupplier = mapSupplier;
    }
    
    MapPropertyResolver() {
        this(LinkedHashMap::new);
    }
    
    @Override
    public <T> Optional<T> resolve(PropertySource source, String key, Type type, ValueResolver resolver) {
        ParameterizedType pType = (ParameterizedType)type;
        
        Map map = mapSupplier.get();
        Set<String> keys = new HashSet<>();
        source.subset(key).forEach((k, v) -> {
            int index = k.indexOf('.');
            k = index == -1 ? k : k.substring(0, index);
            if (keys.add(k)) {
                map.put(k, resolver.resolve(source, key + "." + k, pType.getActualTypeArguments()[1], resolver).get());
            }
        });
        
        return Optional.of((T)map);
    }
}
