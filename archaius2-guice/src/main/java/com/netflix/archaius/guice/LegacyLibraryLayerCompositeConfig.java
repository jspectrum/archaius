package com.netflix.archaius.guice;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.netflix.archaius.api.Config;
import com.netflix.archaius.api.ConfigManager;
import com.netflix.archaius.api.config.CompositeConfig;
import com.netflix.archaius.api.exceptions.ConfigException;
import com.netflix.archaius.config.AbstractConfig;
import com.netflix.config.api.Layers;

class LegacyLibraryLayerCompositeConfig extends AbstractConfig implements CompositeConfig {

    private final ConfigManager configManager;

    public LegacyLibraryLayerCompositeConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }
    
    @Override
    public Object getRawProperty(String key) {
        throw new IllegalStateException();
    }

    @Override
    public boolean containsKey(String key) {
        throw new IllegalStateException();
    }

    @Override
    public boolean isEmpty() {
        throw new IllegalStateException();
    }

    @Override
    public Iterator<String> getKeys() {
        throw new IllegalStateException();
    }

    @Override
    public boolean addConfig(String name, Config child) throws ConfigException {
        configManager.addConfigToLayer(Layers.LIBRARIES, name, child);
        return false;
    }

    @Override
    public void replaceConfig(String name, Config child) throws ConfigException {
        throw new IllegalStateException();
    }

    @Override
    public void addConfigs(LinkedHashMap<String, Config> configs) throws ConfigException {
        configs.forEach((name, config) -> addConfig(name, config));
    }

    @Override
    public void replaceConfigs(LinkedHashMap<String, Config> configs) throws ConfigException {
        throw new IllegalStateException();
    }

    @Override
    public Config removeConfig(String name) {
        throw new IllegalStateException();
    }

    @Override
    public Config getConfig(String name) {
        throw new IllegalStateException();
    }

    @Override
    public Collection<String> getConfigNames() {
        throw new IllegalStateException();
    }
}
