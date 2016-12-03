package com.netflix.archaius.api;

import java.lang.reflect.Type;

public interface StringConverter {
    Object convert(String value, Type type, StringConverter converter);
}
