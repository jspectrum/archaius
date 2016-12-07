package com.netflix.archaius.creator;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.netflix.archaius.api.Creator;

public class MapTypeCreator implements Creator<Map<?, ?>> {

    private final Supplier<Map<String, Creator<?>>> mapSupplier;
    private final Supplier<Creator<?>> elementSupplier;
    private Map<String, Creator<?>> data;

    public MapTypeCreator(Supplier<Map<String, Creator<?>>> mapSupplier, Supplier<Creator<?>> elementSupplier) {
        this.mapSupplier = mapSupplier;
        this.elementSupplier = elementSupplier;
    }

    @Override
    public void onProperty(String key, Supplier<Object> value) {
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
            .onProperty(remainder, value);
    }

    @Override
    public Map<?, ?> create() {
        if (data == null) {
            return null;
        }
        return Collections.unmodifiableMap(data.entrySet().stream().collect(Collectors.toMap(
            entry -> entry.getKey(),
            entry -> entry.getValue().create())));
    }

}
