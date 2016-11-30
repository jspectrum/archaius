package com.netflix.archaius.sources;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.netflix.archaius.api.PropertySource;

/**
 * 
 */
public class PollingPropertySource implements PropertySource {
    private final Cancellation cancellation;
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
    
    public interface Cancellation {
        void cancel();
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
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return delegate.getProperty(name);
    }

    @Override
    public void forEach(BiConsumer<String, Object> consumer) {
        delegate.forEach(consumer);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public PropertySource subset(String prefix) {
        return delegate.subset(prefix);
    }

    @Override
    public void addListener(Listener listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeListener(Listener listener) {
        // TODO Auto-generated method stub
        
    }
}
