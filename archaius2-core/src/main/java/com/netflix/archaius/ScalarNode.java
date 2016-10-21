package com.netflix.archaius;

import com.netflix.archaius.api.ConfigNode;

import java.util.Collections;

public abstract class ScalarNode implements ConfigNode {

    public static ScalarNode from(Object value, ConfigNode root) {
        return new ScalarNode() {
            @Override
            public ConfigNode root() {
                return root;
            }
            
            @Override
            public Object value() {
                return value;
            }
        };
    }

    @Override
    public ConfigNode child(String name) {
        return null;
    }

	@Override
	public boolean containsKey(String key) {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Iterable<String> keys() {
		return Collections.emptyList();
	}
}
