package com.netflix.archaius.sources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.netflix.archaius.api.Layer;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.internal.WeakReferenceSet;

public class LayeredPropertySource extends DelegatingPropertySource {

    public LayeredPropertySource(String name) {
        this.name = name;
    }
    
    private final String name;
    private final WeakReferenceSet<Consumer<PropertySource>> listeners = new WeakReferenceSet<>();
    private final AtomicReference<State> state = new AtomicReference<>(new State(Collections.emptyList()));
    
    @Override
    public String getName() {
        return this.name;
    }

    public void addPropertySource(Layer layer, PropertySource source) {
        state.getAndUpdate(current -> {
            List<Element> newEntries = new ArrayList<>(current.elements);
            newEntries.add(Element.create(layer, source));
            newEntries.sort(ByLayerAndInsertionOrder);
            return current.withEntries(Collections.unmodifiableList(newEntries));
        });
        
        source.addListener(listener -> notifyListeners());
    }

    @Override
    public AutoCloseable addListener(Consumer<PropertySource> consumer) {
        return listeners.add(consumer, this);
    }
    
    private void notifyListeners() {
        listeners.forEach(listener -> listener.accept(this));
    }

    @Override
    protected PropertySource delegate() {
        return state.get().source;
    }
    
    private static class Element {
        private static final AtomicInteger insertionOrderCounter = new AtomicInteger();
        
        private final Layer layer;
        private final int insertionOrder;
        private final PropertySource source;
        
        static Element create(Layer layer, PropertySource value) {
            return new Element(layer, value);
        }
        
        private Element(Layer layer, PropertySource source) {
            this.layer = layer;
            this.insertionOrder = insertionOrderCounter.incrementAndGet();
            this.source = source;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 + ((source == null) ? 0 : source.hashCode());
            result = prime * result + ((layer == null) ? 0 : layer.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Element other = (Element) obj;
            if (source == null) {
                if (other.source != null)
                    return false;
            } else if (!source.equals(other.source))
                return false;
            if (layer == null) {
                if (other.layer != null)
                    return false;
            } else if (!layer.equals(other.layer))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Element [layer=" + layer + ", id=" + insertionOrder + ", value=" + source + "]";
        }
    }
    
    private static final Comparator<Element> ByLayerAndInsertionOrder = (Element o1, Element o2) -> {
        if (o1.layer != o2.layer) {
            int result = o1.layer.getOrder() - o2.layer.getOrder();
            if (result != 0) {
                return result;
            }
        }
        
        return o2.insertionOrder - o1.insertionOrder;
    };

    private class State {
        private final List<Element> elements;
        private final PropertySource source;
        
        State(List<Element> entries) {
            SortedMap<String, Object> map = new TreeMap<>();
            
            this.elements = entries;
            this.elements.forEach(element -> element.source.forEach((key, value) -> map.putIfAbsent(key, value)));
            
            source = new ImmutablePropertySource(name, map);
        }
        
        State withEntries(List<Element> entries) {
            return new State(entries);
        }
    }

    @Override
    public Stream<PropertySource> children() {
        return state.get().elements.stream().map(element -> element.source);
    }
}
