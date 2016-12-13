package com.netflix.config.resolver;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.netflix.config.api.PropertyNode;
import com.netflix.config.api.PropertyNode.Resolver;
import com.netflix.config.api.PropertyNode.ResolverLookup;

public class MapResolver implements Resolver<Map<Object, Object>> {
    private final Type keyType;
    private final Type valueType;
    
    public MapResolver(Type keyType, Type valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }
    
    @Override
    public Map<Object, Object> resolve(PropertyNode node, ResolverLookup resolvers) {
        return Collections.unmodifiableMap(node.children().stream()
            .map(key -> {
                System.out.println("key: " + key);
                int index = key.indexOf(".");
                return index == -1 ? key : key.substring(0, index);
            })
            .distinct()
            .collect(Collectors.toMap(
                key -> key,
                key -> resolvers.get(valueType).resolve(node.getChild(key), resolvers))
            ));
    }
}
