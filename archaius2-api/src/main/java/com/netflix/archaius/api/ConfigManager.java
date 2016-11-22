package com.netflix.archaius.api;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

/**
 * Top level configuration that is a composite of several override layers. 
 */
public interface ConfigManager extends Config {
    /**
     * Key used to groups and order configurations into layers (@see Layers).
     * Layers are ordered by natural order such that a lower order has precedence
     * over higher orders.  Within a layer configurations are prioritized by
     * insertion order (or reversed if 'reversed=true')
     */
    public static class Key {
        private final String layerName;
        private final String resourceName;
	    private final int layerOrder;
	    
	    // TODO: after(), before()
	    private Key(String name, String resourceName, int order) {
	        this.layerName = name;
	        this.layerOrder = order;
	        this.resourceName = resourceName;
        }

        public static Key of(String name, int order) {
            return new Key(name, "", order);
        }
        
        public Key resource(String resourceName) {
            return new Key(layerName, resourceName, layerOrder);
        }
        
        public int getOrder() {
            return layerOrder;
        }
        
        public String getLayerName() {
            return layerName;
        }
        
        public String getResourceName() {
            return resourceName;
        }

        public String getFullName() {
            StringBuilder sb = new StringBuilder();
            sb.append(layerName);
            if (!resourceName.isEmpty()) {
                sb.append(":").append(resourceName);
            }
            return sb.toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((layerName == null) ? 0 : layerName.hashCode());
            result = prime * result + layerOrder;
            result = prime * result + ((resourceName == null) ? 0 : resourceName.hashCode());
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
            Key other = (Key) obj;
            if (layerName == null) {
                if (other.layerName != null)
                    return false;
            } else if (!layerName.equals(other.layerName))
                return false;
            if (layerOrder != other.layerOrder)
                return false;
            if (resourceName == null) {
                if (other.resourceName != null)
                    return false;
            } else if (!resourceName.equals(other.resourceName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Key [layerName=" + layerName + ", resourceName=" + resourceName + ", layerOrder=" + layerOrder
                    + "]";
        }
	}
    
    /**
     * Add a named resource to a configuration layer.  The resourceName identifies a base file name
     * without the extension (ex. 'application').  A concrete ConfigManager will have been configured with
     * multiple ConfigReaders, each for a specific file format.  All base files supported by any of the readers
     * will be loaded into this layer.  Configurations are loaded using the default ConfigLoader
     * 
     * @param layer
     * @param resourceName
     */
    void addResourceToLayer(Key layer);

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
    void addResourceToLayer(Key layer, Function<ConfigLoader.Loader, ConfigLoader.Loader> loader);

    /**
     * Add properties to this configuration layer
     * @param layer
     * @param props
     */
    void addConfigToLayer(Key layer, Properties props);
    
    /**
     * Add a named configuration to the configuration layer.
     * @param layer
     * @param layerName
     * @param config
     */
    void addConfigToLayer(Key layer, Config config);

    /**
     * Return a named configuration from the specified layer
     * @param librariesLayer
     * @param layerName
     * @return
     */
    Optional<Config> getConfig(Key layer);

    /**
     * Remove a configuration from the specified layer
     * 
     * @param layer
     * @param layerName
     * @return The config that was removed
     */
    Optional<Config> removeConfig(Key layer);
    
    Iterable<String> getConfigNames();

}
