package com.netflix.archaius.creator;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import com.netflix.archaius.api.TypeCreator;
import com.netflix.archaius.internal.Preconditions;

public class SimpleTypeCreator implements TypeCreator<Object> {
    private final Function<String, ?> converter;
    private Object data;
    
    public SimpleTypeCreator(Function<String, ?> converter, Annotation[] annotations) {
        this.converter = converter;
    }

    @Override
    public void accept(String key, Object value) {
        Preconditions.checkArgument(value != null, "Value cannot be null");
        Preconditions.checkArgument(key.isEmpty(), "Illegal property suffix : " + key);

        if (value.getClass() == String.class) {
            data = converter.apply((String)value);
            return;
        }
        
        throw new IllegalArgumentException("Expecting string value but got " + value.getClass());
    }

    @Override
    public Object get() {
        return data;
    }

}
