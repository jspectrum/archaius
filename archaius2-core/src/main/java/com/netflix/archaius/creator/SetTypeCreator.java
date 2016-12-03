package com.netflix.archaius.creator;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.netflix.archaius.api.TypeCreator;

public class SetTypeCreator implements TypeCreator<Set<?>> {

    private final Function<String, ?> converter;
    private Set<?> data;

    public SetTypeCreator(Function<String, ?> converter, Annotation[] annotations) {
        this.converter = converter;
    }

    @Override
    public void accept(String key, Object value) {
        int index = key.indexOf(".");
        accept(index == -1 ? key : key.substring(0, index-1),
               index == -1 ? key : key.substring(0, index-1),
               value);
    }
    
    public void accept(String key, String remainder, Object value) {
        data = Arrays
                .asList(((String)value).split(","))
                .stream()
                .map(converter::apply)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<?> get() {
        return Collections.unmodifiableSet(data);
    }

}
