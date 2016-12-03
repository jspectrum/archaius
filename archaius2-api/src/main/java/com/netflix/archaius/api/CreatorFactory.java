package com.netflix.archaius.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface CreatorFactory {
    TypeCreator<?> create(Type type, Annotation[] annotations);
}
