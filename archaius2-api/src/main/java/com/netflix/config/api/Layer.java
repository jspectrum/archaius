package com.netflix.config.api;

/**
 * Key used to groups and order configurations into layers (@see Layers).
 * Layers are ordered by natural order such that a lower order has precedence
 * over higher orders.  Within a layer configurations are prioritized by
 * insertion order (or reversed if 'reversed=true')
 */
public final class Layer {
    private final String name;
    private final int order;
    
    // TODO: after(), before()
    private Layer(String name, int order) {
        this.name = name;
        this.order = order;
    }

    public static Layer of(String name, int order) {
        return new Layer(name, order);
    }
    
    public int getOrder() {
        return order;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + order;
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
        Layer other = (Layer) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (order != other.order)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Key [layerName=" + name + ", layerOrder=" + order + "]";
    }
}