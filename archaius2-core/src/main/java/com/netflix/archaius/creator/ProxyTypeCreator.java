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
import java.util.stream.Collectors;

import com.netflix.archaius.api.CreatorFactory;
import com.netflix.archaius.api.TypeCreator;
import com.netflix.archaius.api.annotations.PropertyName;

public class ProxyTypeCreator<T> implements TypeCreator<T> {
    
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

    static class MethodTypeCreator implements TypeCreator<Object> {
        private final Method method;
        private final TypeCreator<?> creator;

        MethodTypeCreator(Method method, TypeCreator<?> creator) {
            this.method = method;
            this.creator = creator;
        }

        @Override
        public void accept(String t, Object u) {
            creator.accept(t, u);
        }

        @Override
        public Object get() {
            return creator.get();
        }
    }
    
    private final Function<Method, String> nameResolver = DEFAULT_NAME_RESOLVER;
    private final Map<String, MethodTypeCreator> methods;
    private final Class<T> type;

    public ProxyTypeCreator(CreatorFactory factory, Class<T> type, Annotation[] annotations) {
        this.type = type;
        
        // FIXME: This code can result in an infinate loop for nested proxies
        this.methods = Arrays.asList(type.getMethods())
            .stream()
            .collect(Collectors.toMap(
                nameResolver, 
                method -> new MethodTypeCreator(method, factory.create(method.getGenericReturnType(), method.getDeclaredAnnotations()))));
    }
    
    @Override
    public void accept(String key, Object value) {
        int index = key.indexOf(".");
        apply(index == -1 ? key : key.substring(0, index), 
              index == -1 ? "" : key.substring(index+1),
              value);
    }

    private void apply(String name, String remainder, Object value) {
        MethodTypeCreator method = methods.get(name);
        if (method == null) {
            // TODO: Log an error/warning?
            return;
        }
        method.creator.accept(remainder, value);
    }
    
    @Override
    public T get() {
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
            Object v = creator.get();
            if (v != null) {
                values.put(creator.method, v);
            }
        });
        
        final InvocationHandler handler = (proxy, method, args) -> {
            Object value = values.get(method);
            if (value != null) {
                return value;
            }
            
            if (method.isDefault()) {
                return temp.unreflectSpecial(method, type)
                        .bindTo(proxy)
                        .invokeWithArguments(args);
            }
            
            if ("toString".equals(method.getName())) {
                StringBuilder sb = new StringBuilder();
                sb.append(type.getSimpleName()).append("[");
                values.forEach((m, v) -> {
                    sb.append(nameResolver.apply(m)).append("='");
                    try {
                        if (v != null) {
                            sb.append(v);
                        } else if (m.isDefault()) {
                            sb.append(temp.unreflectSpecial(m, type)
                                    .bindTo(proxy)
                                    .invokeWithArguments(args));
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
                throw new NoSuchMethodError(method.getName() + " not found on interface " + type.getName());
            }
        };
        return (T)Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, handler);
    }
}
