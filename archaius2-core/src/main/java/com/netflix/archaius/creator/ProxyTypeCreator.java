package com.netflix.archaius.creator;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.netflix.archaius.api.Creator;
import com.netflix.archaius.api.CreatorRegistry;
import com.netflix.archaius.api.annotations.DefaultValue;
import com.netflix.archaius.api.annotations.PropertyName;

public class ProxyTypeCreator<T> implements Creator<T> {
    
    private static Function<Method, String> DEFAULT_NAME_RESOLVER = method -> {
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
    
    private static String decapitalize(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        char c[] = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    static class MethodTypeCreator implements Creator<Object> {
        private final Method method;
        private final Creator<?> creator;

        MethodTypeCreator(Method method, Creator<?> creator) {
            this.method = method;
            this.creator = creator;
            
            DefaultValue defaultValue = method.getAnnotation(DefaultValue.class);
            if (defaultValue != null) {
                this.creator.onProperty("", defaultValue::value);
            }
        }

        @Override
        public void onProperty(String t, Supplier<Object> u) {
            creator.onProperty(t, u);
        }

        @Override
        public Object create() {
            return creator.create();
        }
    }
    
    private final Function<Method, String> nameResolver = DEFAULT_NAME_RESOLVER;
    private final Map<String, MethodTypeCreator> methods;
    private final Class<T> type;

    public ProxyTypeCreator(CreatorRegistry factory, Class<T> type, Annotation[] annotations) {
        this.type = type;
        
        // FIXME: This code can result in an infinite loop for nested proxies
        this.methods = Arrays.asList(type.getMethods())
            .stream()
            .collect(Collectors.toMap(
                nameResolver, 
                method -> new MethodTypeCreator(method, factory.get(method.getGenericReturnType(), method.getDeclaredAnnotations()))));
    }
    
    @Override
    public void onProperty(String key, Supplier<Object> value) {
        int index = key.indexOf(".");
        apply(index == -1 ? key : key.substring(0, index), 
              index == -1 ? "" : key.substring(index+1),
              value);
    }

    private void apply(String name, String remainder, Supplier<Object> value) {
        MethodTypeCreator method = methods.get(name);
        if (method == null) {
            // TODO: Log an error/warning?
            return;
        }
        method.creator.onProperty(remainder, value);
    }
    
    @Override
    public T create() {
        // Hack so that default interface methods may be called from a proxy
        final MethodHandles.Lookup temp;
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                    .getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            temp = constructor.newInstance(type, MethodHandles.Lookup.PRIVATE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temporary object for " + type.getTypeName(), e);
        }

        final Map<Method, Object> values = new HashMap<>();
        methods.forEach((key, creator) -> {
            Object v = creator.create();
            if (v != null) {
                values.put(creator.method, v);
            }
        });
        
        final InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object value = values.get(method);
                if (value != null) {
                    return value;
                } else if (method.isDefault()) {
                    return temp.unreflectSpecial(method, type)
                            .bindTo(proxy)
                            .invokeWithArguments(args);
                } else if ("toString".equals(method.getName())) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(type.getSimpleName()).append("[");
                    values.forEach((m, v) -> {
                        sb.append(nameResolver.apply(m)).append("='");
                        try {
                            sb.append(invoke(proxy, m, args));
                        } catch (Throwable e1) {
                            sb.append(e1.getMessage());
                        }
                        sb.append("'");
                        sb.append(", ");
                    });
                    sb.append("]");
                    return sb.toString();
                } else {
                    throw new NoSuchMethodError(method.getName() + " not found on interface " + type.getName());
                }
            }
        };
        return (T)Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, handler);
    }
}
