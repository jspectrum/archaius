package com.netflix.archaius.creator;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.netflix.archaius.api.Collector;

public class MapTypeCreator implements Collector<Map<?, ?>> {

    private final Supplier<Map<String, Collector<?>>> mapSupplier;
    private final Supplier<Collector<?>> elementSupplier;
    private Map<String, Collector<?>> data;

    public MapTypeCreator(Supplier<Map<String, Collector<?>>> mapSupplier, Supplier<Collector<?>> elementSupplier) {
        this.mapSupplier = mapSupplier;
        this.elementSupplier = elementSupplier;
    }

    @Override
    public void accept(String key, Supplier<Object> value) {
        int index = key.indexOf(".");
        accept(index == -1 ? key : key.substring(0, index-1),
               index == -1 ? ""  : key.substring(index+1),
               value);
    }
    
    public void accept(String key, String remainder, Supplier<Object> value) {
        if (data == null) {
            data = mapSupplier.get();
        }
        
        data.computeIfAbsent(key, k -> elementSupplier.get())
            .accept(remainder, value);
    }

    @Override
    public Map<?, ?> get() {
        if (data == null) {
            return null;
        }
        return Collections.unmodifiableMap(data.entrySet().stream().collect(Collectors.toMap(
            entry -> entry.getKey(),
            entry -> entry.getValue().get())));
    }

}
