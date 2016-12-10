package com.netflix.archaius.sources;

import org.apache.commons.lang3.StringUtils;

import com.netflix.archaius.api.PropertySource;

public class SourcePrinter {
    int indent = -1;
    
    public SourcePrinter() {
        
    }
    
    public void onPropertySource(PropertySource source) {
        System.out.println(StringUtils.repeat(" ", indent) + source.getName());
        indent++;
        source.forEach((key, value) -> onProperty(key, value.toString()));
        source.children().forEach(s -> onPropertySource(s));
        indent--;
    }

    public void onProperty(String key, String value) {
        System.out.println(StringUtils.repeat(" ", indent) + key + "=" + value);
    }
    
    public void finish(SourcePrinter printer) {
    }
    
}
