package com.netflix.archaius.sources;

import org.apache.commons.lang3.StringUtils;

import com.netflix.archaius.api.PropertySource;

public class SourcePrinter {
    int indent = -1;
    
    public SourcePrinter() {
        
    }
    
    public void onPropertySource(PropertySource t) {
        indent++;
        System.out.println(StringUtils.repeat(" ", indent) + t.getName());
        if (t instanceof CompositePropertySource) {
            ((CompositePropertySource)t).children().forEach(s -> onPropertySource(s));
        }
        else {
            indent++;
            t.stream().forEach(entry -> onProperty(entry.getKey(), entry.getValue().toString()));
            indent--;
        }
        indent--;
    }

    public void onProperty(String key, String value) {
        System.out.println(StringUtils.repeat(" ", indent) + key + "=" + value);
    }
    
    public void finish(SourcePrinter printer) {
    }
    
}
