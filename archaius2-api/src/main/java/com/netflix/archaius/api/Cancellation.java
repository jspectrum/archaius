package com.netflix.archaius.api;

public interface Cancellation {
    void cancel();
    
    public static Cancellation empty() {
        return () -> {};
    }
}
