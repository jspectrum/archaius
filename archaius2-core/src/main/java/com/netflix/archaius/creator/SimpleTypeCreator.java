package com.netflix.archaius.creator;

import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Supplier;

import com.netflix.archaius.api.Creator;
import com.netflix.archaius.internal.Preconditions;

public class SimpleTypeCreator implements Creator<Object> {
    private final Function<String, ?> converter;
    private Object data;
    
    public SimpleTypeCreator(Function<String, ?> converter, Annotation[] annotations) {
        this.converter = converter;
    }

    @Override
    public void onProperty(String key, Supplier<Object> supplier) {
        Preconditions.checkArgument(supplier != null, "Value cannot be null");
        Preconditions.checkArgument(key.isEmpty(), "Illegal property suffix : " + key);

        Object value = supplier.get();
        if (value.getClass() == String.class) {
            data = converter.apply((String)value);
            return;
        }
        
        throw new IllegalArgumentException("Expecting string value but got " + value.getClass());
    }

    @Override
    public Object create() {
        return data;
    }

}
