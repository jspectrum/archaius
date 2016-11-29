package com.netflix.archaius.api;

public interface DynamicPropertySource {
    interface Listener {
        void onUpdate(PropertySource propertySource);
    }
    
    void addListener(Listener listener);
    void removeListener(Listener listener);
}
