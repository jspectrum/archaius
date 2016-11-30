package com.netflix.archaius.matchers;

import java.lang.reflect.Type;

import com.netflix.archaius.api.Matcher;

public final class Matchers {
    public static Matcher isInterface() {
        return new Matcher() {
            @Override
            public boolean matches(Type type) {
                if (type instanceof Class<?>) {
                    return ((Class<?>)type).isInterface();
                }
                return false;
            }
        };
    }
    
    private Matchers() { }
}
