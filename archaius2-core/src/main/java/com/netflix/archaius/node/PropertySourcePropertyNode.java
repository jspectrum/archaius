package com.netflix.archaius.node;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import com.netflix.archaius.api.PropertyNode;
import com.netflix.archaius.api.PropertySource;

public class PropertySourcePropertyNode implements PropertyNode {
    private String path = "";
    private PropertySource source;
    
    public PropertySourcePropertyNode(PropertySource source) {
        this(source, "");
    }
    
    public PropertySourcePropertyNode(PropertySource source, String path) {
        this.source = source;
        this.path = path;
    }
    
    @Override
    public Optional<?> getValue() {
        return source.getProperty(path);
    }

    @Override
    public PropertyNode getNode(String key) {
        return new PropertySourcePropertyNode(source, path.isEmpty() ? key : path + "." + key);
    }
    
    @Override
    public Stream<String> keys() {
        return source.keys(path);
    }
}
