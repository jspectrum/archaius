package com.netflix.archaius.api;

public interface TypeMatcher {
    boolean matches(TypeToken type);
}
