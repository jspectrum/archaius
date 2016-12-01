package com.netflix.archaius.resolvers;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.netflix.archaius.api.PropertySource;
import com.netflix.archaius.api.ValueResolver;
import com.netflix.archaius.api.annotations.PropertyName;

public class InterfacePropertyResolver implements ValueResolver {
    public static interface PropertyNameResolver {
        String resolve(Method method);
    }
    
    private static PropertyNameResolver DEFAULT_NAME_RESOLVER = method -> {
        final PropertyName nameAnnot = method.getAnnotation(PropertyName.class); 
        if (nameAnnot != null) {
            return nameAnnot.name();
        }
        
        String name;
        if (method.getName().startsWith("get")) {
            name = method.getName().substring("get".length());
        } else if (method.getName().startsWith("is")) {
            name = method.getName().substring("is".length());
        } else if (method.getName().startsWith("with")) {
            name = method.getName().substring("with".length());
        } else {
            name = method.getName();
        }
        
        return decapitalize(name);
    };
    
    public static String decapitalize(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        char c[] = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    private final PropertyNameResolver nameResolver = DEFAULT_NAME_RESOLVER;
    
    @Override
    public <T> Optional<T> resolve(PropertySource source, String key, Type type, ValueResolver resolver) {
        return resolve(source, key, (Class<?>)type, resolver);
    }
    
    private <T> Optional<T> resolve(PropertySource source, String key, Class<?> cls, ValueResolver resolver) {
        // Hack so that default interface methods may be called from a proxy
        final MethodHandles.Lookup temp;
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                    .getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            temp = constructor.newInstance(cls, MethodHandles.Lookup.PRIVATE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temporary object for " + cls.getTypeName(), e);
        }
        
        // Resolve value for every field
        final Map<Method, Optional<Object>> values = new HashMap<>();
        for (Method method : cls.getMethods()) {
            Optional<Object> value = resolver.resolve(source, key + "." + nameResolver.resolve(method), method.getGenericReturnType(), resolver);
            values.put(method, value);
        }
        
        final InvocationHandler handler = (proxy, method, args) -> {
            Optional<Object> value = values.get(method);
            if (value != null) {
                if (!value.isPresent() && method.isDefault()) {
                    return temp.unreflectSpecial(method, cls)
                            .bindTo(proxy)
                            .invokeWithArguments();
                }
                return value.get();
            }
            
            if ("toString".equals(method.getName())) {
                StringBuilder sb = new StringBuilder();
                sb.append(cls.getSimpleName()).append("[");
                values.forEach((m, v) -> {
                    sb.append(nameResolver.resolve(m)).append("='");
                    try {
                        if (v != null) {
                            if (!v.isPresent() && m.isDefault()) {
                                v = Optional.ofNullable(temp.unreflectSpecial(m, cls)
                                        .bindTo(proxy)
                                        .invokeWithArguments());
                            }
                            sb.append(v.orElse(null));
                        } else {
                            sb.append("null");
                        }
                    } catch (Throwable e) {
                        sb.append(e.getMessage());
                    }
                    sb.append("'");
                    sb.append(", ");
                });
                sb.append("]");
                return sb.toString();
            } else {
                throw new NoSuchMethodError(method.getName() + " not found on interface " + cls.getName());
            }
        };
        return Optional.of((T)Proxy.newProxyInstance(cls.getClassLoader(), new Class[] { cls }, handler));
    }
}
