package com.netflix.archaius.api;

import java.util.Optional;
import java.util.stream.Stream;

public interface PropertyNode {
    Optional<?> getValue();
    
    default PropertyNode getNode(String key) {
        throw new IllegalStateException();
    }
    
    default Stream<String> keys() {
        return Stream.empty();
    }
}
