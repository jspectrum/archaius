package com.netflix.archaius.api;

import com.netflix.archaius.api.ConfigManager.Key;

/**
 * Set of pre-defined configuration override layers that should be realistic for most applications
 * and tests
 */
public final class Layers {
    /**
     * Reserved for test specific configurations.  This layer should only be installed for unit
     * tests and will take precedence over any other layer
     */
    public static final Key TEST                  = Key.of("test",        180);
    
    /**
     * Layer reserved for code override and is normally attached to a settable config
     */
    public static final Key OVERRIDE              = Key.of("override",    170);

    /**
     * Layer with immutable system properties (-D)
     */
    public static final Key SYSTEM                = Key.of("sys",         160);
    
    /**
     * Layer with immutable environment properties
     */
    public static final Key ENVIRONMENT           = Key.of("env",         150);
    
    /**
     * Layer reserved for remove configuration overrides from persistent storage
     */
    public static final Key REMOTE_OVERRIDE       = Key.of("remote",      130);
    
    /**
     * Override for application configuration loaded in property files.
     */
    public static final Key APPLICATION_OVERRIDE  = Key.of("app_override",140);

    /**
     * Layer to be used by the application for application specific configurations
     * and allows for the application to override any configuration loaded by libraries
     */
    public static final Key APPLICATION           = Key.of("app",         120);
    
    /**
     * Layer into which any class or 'library' may load its configuration
     */
    public static final Key LIBRARIES             = Key.of("libraries",   110);
    
    /**
     * Layer into which any class or 'library' may specify default values that can be
     * overwritten by libraries
     */
    public static final Key DEFAULT               = Key.of("default",     100);
}
