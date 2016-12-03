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
import java.util.function.Supplier;

import com.netflix.archaius.api.OrderedKey;
import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.internal.GarbageCollectingSet;

public class OrderedPropertySource extends DelegatingPropertySource {

    private static class Element {
        private final OrderedKey layer;
        private final int insertionOrder;
        
        private final PropertySource value;
        
        private static final AtomicInteger insertionOrderCounter = new AtomicInteger();
        
        static Element create(OrderedKey layer, PropertySource value) {
            return new Element(layer, value);
        }
        
        private Element(OrderedKey layer, PropertySource value) {
            this.layer = layer;
            this.insertionOrder = insertionOrderCounter.incrementAndGet();
            this.value = value;
        }
        
        public PropertySource getPropertySource() {
            return value;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 + ((value == null) ? 0 : value.hashCode());
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
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
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
            return "Element [layer=" + layer + ", id=" + insertionOrder + ", value=" + value + "]";
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
            SortedMap<String, Supplier<Object>> map = new TreeMap<>();
            
            this.elements = entries;
            this.elements
                .stream()
                .map(Element::getPropertySource)
                .forEach(
                    source -> source.forEach((k, v) -> map.putIfAbsent(k, v))
                );
            
            source = new ImmutablePropertySource(name, map);
        }
        
        State withEntries(List<Element> entries) {
            return new State(entries);
        }
    }

    public OrderedPropertySource(String name) {
        this.name = name;
    }
    
    private final String name;
    private final GarbageCollectingSet<Consumer<PropertySource>> listeners = new GarbageCollectingSet<>();
    private final AtomicReference<State> state = new AtomicReference<>(new State(Collections.emptyList()));
    
    @Override
    public String getName() {
        return this.name;
    }

    public void addPropertySource(OrderedKey layer, PropertySource source) {
        state.getAndUpdate(current -> {
            List<Element> newEntries = new ArrayList<>(current.elements);
            newEntries.add(Element.create(layer, source));
            newEntries.sort(ByLayerAndInsertionOrder);
            return current.withEntries(Collections.unmodifiableList(newEntries));
        });
        
        source.addListener(listener -> notifyListeners());
    }

    @Override
    public Runnable addListener(Consumer<PropertySource> consumer) {
        return listeners.add(consumer, this);
    }
    
    protected void notifyListeners() {
        listeners.forEach(listener -> listener.accept(this));
    }

    @Override
    protected PropertySource delegate() {
        return state.get().source;
    }
}
