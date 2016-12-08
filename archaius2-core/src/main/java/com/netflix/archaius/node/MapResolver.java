package com.netflix.archaius.node;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Collectors;

import com.netflix.archaius.api.PropertyNode;
import com.netflix.archaius.api.Resolver;
import com.netflix.archaius.api.ResolverLookup;

public class MapResolver implements Resolver<Map<Object, Object>> {
    private final Type keyType;
    private final Type valueType;
    
    public MapResolver(Type keyType, Type valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }
    
    @Override
    public Map<Object, Object> resolve(PropertyNode node, ResolverLookup resolvers) {
        return node.keys()
            .map(key -> {
                int index = key.indexOf(".");
                return index == -1 ? key : key.substring(0, index);
            })
            .distinct()
            .collect(Collectors.toMap(
                key -> key.toString(),
                key -> resolvers.get(valueType).resolve(node.getNode(key), resolvers))
            );
    }

}
