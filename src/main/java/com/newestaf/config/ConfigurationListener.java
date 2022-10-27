package com.newestaf.config;

public interface ConfigurationListener {

    // Validate and possibly modify or veto a potential change to this configuration.
    // Simply return newVal to proceed with the proposed configuration change.
    public Object onConfigurationValidate(ConfigurationManager configurationManager, String key, Object oldVal, Object newVal);

    public Object onConfigurationChanged(ConfigurationManager configurationManager, String key, Object oldVal, Object newVal);

}

