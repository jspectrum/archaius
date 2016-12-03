package com.netflix.archaius.creator;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.netflix.archaius.api.Collector;

public class ListTypeCreator implements Collector<List<?>> {

    private final Function<String, ?> converter;
    private List<?> data;

    public ListTypeCreator(Function<String, ?> converter, Annotation[] annotations) {
        this.converter = converter;
    }

    @Override
    public void accept(String key, Supplier<Object> supplier) {
        int index = key.indexOf(".");
        accept(index == -1 ? key : key.substring(0, index-1),
               index == -1 ? "" : key.substring(index + 1),
               supplier);
    }
    
    private void accept(String key, String remainder, Supplier<Object> value) {
        if (remainder.isEmpty()) {
            data = Arrays
                    .asList(((String)value.get()).split(","))
                    .stream()
                    .map(converter::apply)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<?> get() {
        return (data == null)
            ? Collections.emptyList()
            : Collections.unmodifiableList(data);
    }
}
