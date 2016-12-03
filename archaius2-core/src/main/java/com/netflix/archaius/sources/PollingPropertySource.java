package com.netflix.archaius.sources;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.netflix.archaius.api.Cancellation;
import com.netflix.archaius.api.PropertySource;

/**
 * 
 */
public class PollingPropertySource extends DelegatingPropertySource {
    private final Cancellation cancellation;
    private final CopyOnWriteArrayList<Consumer<PropertySource>> listeners = new CopyOnWriteArrayList<>();
    private volatile PropertySource delegate;
    
    /**
     * Async supplier of PropertySource instances
     */
    public interface PropertySourceSupplier {
        Cancellation get(Consumer<PropertySource> consumer);
    }
    
    /**
     * Strategy driving when to poll
     */
    public interface Strategy {
        Cancellation start(Runnable runnable);
    }
    
    public PollingPropertySource(Strategy strategy, Consumer<Consumer<PropertySource>> source) {
        // TODO: Cancel the source consumer
        cancellation = strategy.start(() -> source.accept(s -> setSource(s)));
    }
    
    private void setSource(PropertySource source) {
        this.delegate = source;
    }
    
    public void shutdown() {
        cancellation.cancel();
    }

    @Override
    public Cancellation addListener(Consumer<PropertySource> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    protected void notifyListeners() {
        listeners.forEach(listener -> listener.accept(this));
    }
    
    @Override
    protected PropertySource delegate() {
        return delegate;
    }
}
