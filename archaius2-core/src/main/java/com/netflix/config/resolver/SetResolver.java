package com.netflix.config.resolver;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.netflix.config.api.PropertyNode;
import com.netflix.config.api.PropertyNode.Resolver;
import com.netflix.config.api.PropertyNode.ResolverLookup;

public class SetResolver implements Resolver<Set<?>> {

    private Type elementType;
    
    public SetResolver(Type elementType) {
        this.elementType = elementType;
    }
    
    @Override
    public Set<?> resolve(PropertyNode node, ResolverLookup resolvers) {
        return node.getValue().map(value -> {
            if (value instanceof String) {
                return Collections.unmodifiableSet(Arrays.asList(((String)value).split(","))
                    .stream()
                    .map(element -> resolvers.get(elementType).resolve(new PropertyNode() {
                        @Override
                        public Optional<?> getValue() {
                            return Optional.of(element);
                        }
                    }, resolvers))
                    .collect(Collectors.toSet()));
            } else {
                throw new IllegalArgumentException();
            }
        }).orElse(Collections.emptySet());
    }
}
