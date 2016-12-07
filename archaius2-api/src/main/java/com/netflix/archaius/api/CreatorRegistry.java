package com.netflix.archaius.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface CreatorRegistry {
    Creator<?> get(Type type, Annotation[] annotations);
    
    default <T> Creator<T> create(Class<T> type, Annotation[] annotations) {
        return (Creator<T>) get((Type)type, annotations);
    }
    
    default <T> Creator<T> create(Class<T> type) {
        return (Creator<T>) get((Type)type, type.getAnnotations());
    }
}
