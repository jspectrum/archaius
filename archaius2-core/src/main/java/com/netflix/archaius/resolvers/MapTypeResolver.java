package com.netflix.archaius.resolvers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.netflix.archaius.api.Context;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.TypeResolver;

public class MapTypeResolver implements TypeResolver {
    private final Supplier<Map> mapSupplier;

    MapTypeResolver(Supplier<Map> mapSupplier) {
        this.mapSupplier = mapSupplier;
    }
    
    MapTypeResolver() {
        this(LinkedHashMap::new);
    }
    
    @Override
    public <T> Optional<T> resolve(Context context, PropertySource source, String key, Type type) {
        ParameterizedType pType = (ParameterizedType)type;
        Type valueType = pType.getActualTypeArguments()[1];
        
        Map map = mapSupplier.get();
        Set<String> keys = new HashSet<>();
        source.forEach(key, (k, v) -> {
            // <key>.<map_key>[.<remainder>]
            System.out.println(key);
            System.out.println(k);;
            int index = k.indexOf('.', key.length() + 1);
            String mapKey = index == -1 ? k.substring(key.length() + 1) : k.substring(key.length()+1, index);
            String valuePrefix = index == -1 ? k : k.substring(0, index);
            if (keys.add(k)) {
                context.resolve(source, k, valueType).ifPresent(value -> map.put(mapKey, value));
            }
        });
        
        return Optional.of((T)map);
    }
}
