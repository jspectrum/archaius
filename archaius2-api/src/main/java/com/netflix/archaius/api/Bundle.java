package com.netflix.archaius.api;

import java.util.List;
import java.util.function.Function;

public class Bundle {
    public Bundle(String name, Function<String, List<String>> cascadeGenerator) {
        this.name = name;
        this.cascadeGenerator = cascadeGenerator;
    }
    
    private final String name;
    private final Function<String, List<String>> cascadeGenerator;
    
    public Function<String, List<String>> getCascadeGenerator() {
        return cascadeGenerator;
    }
    
    public String getName() {
        return name;
    }
}
