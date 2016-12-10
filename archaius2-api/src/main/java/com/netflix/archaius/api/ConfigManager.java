package com.netflix.archaius.api;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

/**
 * Top level configuration that is a composite of several override layers. 
 */
public interface ConfigManager extends Config {
    /**
     * Add a named resource to a configuration layer.  The resourceName identifies a base file name
     * without the extension (ex. 'application').  A concrete ConfigManager will have been configured with
     * multiple ConfigReaders, each for a specific file format.  All base files supported by any of the readers
     * will be loaded into this layer.  Configurations are loaded using the default ConfigLoader
     * 
     * @param layer
     * @param resourceName
     */
    void addResourceToLayer(Layer layer, String resourceName);

    /**
     * Add a named resource to a configuration layer.  The resourceName identifies a base file name
     * without the extension (ex. 'application').  A concrete ConfigManager will have been configured with
     * multiple ConfigReaders, each for a specific file format.  All base files supported by any of the readers
     * will be loaded into this layer.  The {@link ConfigLoader} may be customized using the provided function
     * 
     * @param layer
     * @param resource
     * @param loader
     */
    void addResourceToLayer(Layer layer, String resourceName, Function<ConfigLoader.Loader, ConfigLoader.Loader> loader);

    /**
     * Add properties to this configuration layer
     * @param layer
     * @param props
     */
    void addConfigToLayer(Layer layer, String name, Properties props);
    
    /**
     * Add a named configuration to the configuration layer.
     * @param layer
     * @param layerName
     * @param config
     */
    void addConfigToLayer(Layer layer, String name, Config config);

    /**
     * Return a named configuration from the specified layer
     * @param librariesLayer
     * @param layerName
     * @return
     */
    Optional<Config> getConfig(Layer layer, String name);

    /**
     * Remove a configuration from the specified layer
     * 
     * @param layer
     * @param layerName
     * @return The config that was removed
     */
    Optional<Config> removeConfig(Layer layer, String name);

    Iterable<String> getConfigNames();

    @Deprecated
    ConfigLoader getConfigLoader();

}
