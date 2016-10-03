package com.netflix.archaius;

import com.netflix.archaius.api.DataNode;

public class ScalarNode implements DataNode {

    public static ScalarNode from(Object value, DataNode root) {
        return new ScalarNode(value, root);
    }

    private final Object value;
    private final DataNode root;
    
    private ScalarNode(Object value, DataNode root) {
        this.value = value;
        this.root = root;
    }
    
    @Override
    public DataNode child(String name) {
        return null;
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public DataNode root() {
        return root;
    }
}
