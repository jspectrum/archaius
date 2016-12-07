package com.netflix.archaius.creator;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.netflix.archaius.api.Creator;

public class SetTypeCreator implements Creator<Set<?>> {

    private final Function<String, ?> converter;
    private Set<?> data;

    public SetTypeCreator(Function<String, ?> converter, Annotation[] annotations) {
        this.converter = converter;
    }

    @Override
    public void onProperty(String key, Supplier<Object> supplier) {
        int index = key.indexOf(".");
        accept(index == -1 ? key : key.substring(0, index-1),
               index == -1 ? "" : key.substring(index + 1),
               supplier);
    }
    
    public void accept(String key, String remainder, Supplier<Object> supplier) {
        if (remainder.isEmpty()) {
            data = Arrays
                    .asList(((String)supplier.get()).split(","))
                    .stream()
                    .map(converter::apply)
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public Set<?> create() {
        return Collections.unmodifiableSet(data);
    }

}
