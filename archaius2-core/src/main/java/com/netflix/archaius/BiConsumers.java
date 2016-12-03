package com.netflix.archaius;

import java.io.PrintStream;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class BiConsumers {
    private BiConsumers() {
        
    }
    
    public static BiConsumer<String, Supplier<Object>> print(PrintStream ps) {
        return (k, sv) -> ps.println(k + "=" + sv.get());
    }
}
