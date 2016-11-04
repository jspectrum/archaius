package com.netflix.archaius.api;

import java.util.function.Function;

/**
 * Capture the entire archaius configuration. 
 */
public interface ConfigManager {
    /**
     * Builder used to customize the ArchaiusConfig during archaius bootstrapping
     */
    interface Builder {
        /**
         * Add a composite layer
         * @param key
         * @return Chainable builder
         */
        Builder addCompositeLayer(String layer);
        
        /**
         * Add a layer with a fixed configuration 
         * 
         * @param key
         * @param config
         * @return Chainable builder
         */
        Builder addLayer(String layer, Config config);
        
        /**
         * Set the cascade strategy to use when loading resources.  
         * @param strategy
         * @return Chainable builder
         */
        Builder setCascadeStrategy(CascadeStrategy strategy);
        
        /**
         * Add a reader for a specific format
         * @param reader
         * @return Chainable builder
         */
        Builder addReader(ConfigReader reader);

        Builder addResourceToLayer(String layer, String resourceName);

        Builder addConfigToLayer(String layer, String name, Config config);
        
        Builder addConfigToLayer(String layer, String name, Function<Config, Config> config);
        
        ConfigManager build();
    }
    
    /**
     * Return the top level config object.  This object has the entire configuration hieararchy and
     * should be the one used to access properties.
     * 
     * @return Top level config object
     */
    Config getConfig();
    
    /**
     * Load a configuration into the libraries layer
     * @param name - Resource name
     */
    Config getConfigLayer(String name);

    Config addResourceToLayer(String layer, String resourceName);

    Config addConfigToLayer(String layer, String name, Config config);
    
    Config addConfigToLayer(String layer, String resourceName, Function<ConfigLoader.Loader, ConfigLoader.Loader> loader);
    
    /**
     * @return Return the default decoder.  The decoder will include any registered {@link MatchingDecoder}.
     */
    Decoder getDecoder();
}
