package com.netflix.archaius;

import com.netflix.archaius.ConfigProxyFactory.MethodInvoker;
import com.netflix.archaius.api.ConfigNode;
import com.netflix.archaius.api.TypeDecoder;
import com.netflix.archaius.api.TypeDecoders;
import com.netflix.archaius.api.TypeToken;
import com.netflix.archaius.api.annotations.PropertyName;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ProxyDecoder implements TypeDecoder {

    private final Function<Method, String> methodToPropertyName = (method) -> {
        final String verb;
        if (method.getName().startsWith("get")) {
            verb = "get";
        } else if (method.getName().startsWith("is")) {
            verb = "is";
        } else {
            verb = "";
        }
        
        final PropertyName nameAnnot = method.getAnnotation(PropertyName.class); 
        return nameAnnot != null && nameAnnot.name() != null
            ? nameAnnot.name()
            : Character.toLowerCase(method.getName().charAt(verb.length())) + method.getName().substring(verb.length() + 1);

    };
    
    ProxyDecoder() {
        
    }
    
    @Override
    public Object decode(TypeToken type, ConfigNode node, TypeDecoders decoders) {
        final Map<Method, MethodInvoker<?>> invokers = new HashMap<>();

        for (Method method : type.getRawType().getMethods()) {
            ConfigNode elementNode = node.child(methodToPropertyName.apply(method));
        }
        
        // TODO Auto-generated method stub
        return null;
    }

}
