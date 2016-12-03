package com.netflix.archaius.sources;

import java.util.function.Consumer;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.internal.GarbageCollectingSet;

/**
 * 
 */
public class PollingPropertySource extends DelegatingPropertySource {
    private final Runnable cancellation;
    private final GarbageCollectingSet<Consumer<PropertySource>> listeners = new GarbageCollectingSet<>();
    private volatile PropertySource delegate;
    
    /**
     * Async supplier of PropertySource instances
     */
    public interface PropertySourceSupplier {
        Runnable get(Consumer<PropertySource> consumer);
    }
    
    /**
     * Strategy driving when to poll
     */
    public interface Strategy {
        Runnable start(Runnable runnable);
    }
    
    public PollingPropertySource(Strategy strategy, Consumer<Consumer<PropertySource>> source) {
        // TODO: Cancel the source consumer
        cancellation = strategy.start(() -> source.accept(s -> setSource(s)));
    }
    
    private void setSource(PropertySource source) {
        this.delegate = source;
    }
    
    public void shutdown() {
        cancellation.run();
    }

    @Override
    public Runnable addListener(Consumer<PropertySource> listener) {
        return listeners.add(listener, this);
    }

    protected void notifyListeners() {
        listeners.forEach(listener -> listener.accept(this));
    }
    
    @Override
    protected PropertySource delegate() {
        return delegate;
    }
}
