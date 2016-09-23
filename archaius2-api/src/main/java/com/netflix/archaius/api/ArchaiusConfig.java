package com.netflix.archaius.api;

import com.netflix.archaius.api.ArchaiusConfig.Configurator;
import com.netflix.archaius.api.config.CompositeConfig;
import com.netflix.archaius.api.config.SettableConfig;

import java.util.function.Function;

/**
 * Capture the entire archaius configuration. 
 */
public interface ArchaiusConfig {
    /**
     * Builder used to customize the ArchaiusConfig during archaius bootstrapping
     */
    interface Configurator {
        /**
         * Set the cascade strategy to use when loading resources.  
         * @param strategy
         * @return Chainable configurator
         */
        Configurator setCascadeStrategy(CascadeStrategy strategy);

        /**
         * Add a reader for a specific format
         * @param reader
         * @return Chainable configurator
         */
        Configurator addReader(ConfigReader reader);
        
        /**
         * Add a decoder extensions.
         * @param decoder
         * @return Chainable configurator
         */
        Configurator addDecoder(MatchingDecoder decoder);
        
        /**
         * Add a configuration to the defaults layer
         * 
         * @param name
         * @param config
         * @return Chainable configurator
         */
        Configurator addDefaultConfig(String name, Function<Config, Config> config);

        /**
         * Add a configuration to the runtime override layer
         * @param config
         * @return Chainable configurator
         */
        Configurator addOverrideConfig(Config config);

        /**
         * Set the application configuration file name.  The default name is 'application'.
         * @param name
         * @return Chainable configurator
         */
        Configurator setApplicationName(String name);

        /**
         * Load configuration from a resource.  Resources and configs are loaded in the order in 
         * which they are specified.
         * 
         * @param name
         * @return Chainable configurator
         */
        Configurator addApplicationOverrideResource(String name);

        /**
         * Add an application level override.
         * @param uniqueName
         * @param applicationOverride
         * @return Chainable configurator
         */
        Configurator addApplicationOverrideConfig(String uniqueName, Function<Config, Config> applicationOverride);

        /**
         * Add a configuratino to the remove layer override
         * @param uniqueName
         * @param config
         * @return
         */
        Configurator addRemoteConfig(String uniqueName, Function<Config, Config> config);
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
    void loadLibraryConfig(String name);

    /**
     * @return Return the SettableConfig for the overrides layer through which runtime overrides may 
     * be set.  This mechanism should only be used for testing purposes.  Runtime configuration 
     * updates are better implemented using a persisted distributed configuration mechanism.  These 
     * overrides take precedence over ALL other configuration layers
     */
    SettableConfig getOverrideConfig();

    /**
     * Load a Configuration into the libraries layer using the provided loader.  Use this when
     * you wish to customize the default loader.
     * 
     * For example, a custom cascade strategy may be use for loading resource "foo" like so,
     * <pre>
     * {@code 
     * archaius.loadLibraryConfig("foo", loader -> loader.withCascadeStrategy(fooCascadeStrategy));
     * }
     * </pre>
     * 
     * @param name The resource name to look for.  This is also the unique name assigned to the loaded
     * configuration.
     * @param func
     */
    void loadLibraryConfig(String name, Function<ConfigLoader.Loader, ConfigLoader.Loader> func);

    /**
     * Return the 
     * @return
     */
    CompositeConfig getLibrariesConfig();

    /**
     * @return Return the default decoder.  The decoder will include any registered {@link MatchingDecoder}.
     */
    Decoder getDecoder();
}
