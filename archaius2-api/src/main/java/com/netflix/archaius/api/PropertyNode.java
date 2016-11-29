package com.netflix.archaius.api;

import java.util.Optional;

public interface PropertyNode {

    Optional<Object> getValue();

    PropertyNode path(String prefix);

}
