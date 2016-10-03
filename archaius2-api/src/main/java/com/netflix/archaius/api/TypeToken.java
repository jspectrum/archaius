package com.netflix.archaius.api;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class TypeToken {
    public static TypeToken create(Type type) {
        return new TypeToken(type);
    }

    private Type type;
    private Class<?> rawType;

    public TypeToken(Type type) {
        this.type = type;
        this.rawType = getRawType(type);
    }

    public Type getType() {
        return type;
    }
    
    public Class<?> getRawType() {
        return rawType;
    }
    
    static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // type is a normal class.
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of
            // Class.
            // Neal isn't either but suspects some pathological case related
            // to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) {
                throw new IllegalArgumentException(String.format("Expected a Class, but <%s> is of type %s", type,
                    type.getClass().getName()));
            }
            return (Class<?>) rawType;

        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();

        } else if (type instanceof TypeVariable) {
            // we could use the variable's bounds, but that'll won't work if
            // there are multiple.
            // having a raw type that's more general than necessary is okay
            return Object.class;

        } else {
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or " + "GenericArrayType, but <"
                    + type + "> is of type " + type.getClass().getName());
        }
    }
}
