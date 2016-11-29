package com.netflix.archaius.api;

import java.util.Optional;

public class SimplePropertyNode implements PropertyNode {
    private PropertySource source;
    private String path;

    public SimplePropertyNode(PropertySource source) {
        this(source, "");
    }
    
    public SimplePropertyNode(PropertySource source, String path) {
        this.source = source;
        this.path = path;
    }
    
    @Override
    public Optional<Object> getValue() {
        return source.getProperty(path);
    }
    
    @Override
    public SimplePropertyNode path(String prefix) {
        return new SimplePropertyNode(source, path + prefix);
    }

    @Override
    public String toString() {
        return "SimplePropertyNode [source=" + source.getName() + ", path=" + path + "]";
    }
}
