package com.netflix.archaius.api;

import java.lang.reflect.Type;

public interface Matcher {
    boolean matches(Type type);
}
