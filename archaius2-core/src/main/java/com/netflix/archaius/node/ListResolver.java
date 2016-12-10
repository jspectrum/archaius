package com.netflix.archaius.node;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.netflix.archaius.api.PropertyNode;
import com.netflix.archaius.api.PropertyNode.Resolver;
import com.netflix.archaius.api.PropertyNode.ResolverLookup;

public class ListResolver implements Resolver<List<?>> {

    private Type elementType;
    
    public ListResolver(Type elementType) {
        this.elementType = elementType;
    }
    
    @Override
    public List<?> resolve(PropertyNode node, ResolverLookup resolvers) {
        return node.getValue().map(value -> {
            if (value instanceof String) {
                return Collections.unmodifiableList(Arrays.asList(((String)value).split(","))
                    .stream()
                    .map(element -> resolvers.get(elementType).resolve(new PropertyNode() {
                        @Override
                        public Optional<?> getValue() {
                            return Optional.of(element);
                        }
                    }, resolvers))
                    .collect(Collectors.toList()));
            } else {
                throw new IllegalArgumentException();
            }
        }).orElse(Collections.emptyList());
    }
}
