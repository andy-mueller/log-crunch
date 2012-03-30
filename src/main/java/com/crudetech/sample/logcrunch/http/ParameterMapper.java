package com.crudetech.sample.logcrunch.http;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ParameterMapper {
    private final Map<String, String[]> parameters;

    public ParameterMapper(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    public void mapTo(Object o) {
        Method[] methods = o.getClass().getMethods();
        for (Method method : methods) {
            Parameter par = method.getAnnotation(Parameter.class);
            if (par == null) {
                continue;
            }

            Class<?> parameterType = getParameterType(method);
            Method factory = getFactory(parameterType);
            String parameterName = par.value();
            String[] parameterValues = gerParameterValues(parameterName);
            for (String parameterValue : parameterValues) {
                Object parameter = newInstance(factory, parameterValue);
                setValue(method, o, parameter);
            }
        }
    }

    private Object setValue(Method method, Object o, Object parameter) {
        method.setAccessible(true);
        try {
            return method.invoke(o, parameter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object newInstance(Method factory, String parameterValue) {
        try {
            return setValue(factory, null, parameterValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> getParameterType(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new RuntimeException();
        }
        return convertPrimitives(parameterTypes[0]);
    }

    private static final Map<Class<?>, Class<?>> primitiveToWrapper = Collections.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
        {
            put(Boolean.TYPE, Boolean.class);
            put(Integer.TYPE, Integer.class);
            put(Short.TYPE, Short.class);
            put(Long.TYPE, Long.class);
            put(Character.TYPE, Character.class);
            put(Byte.TYPE, Byte.class);
            put(Double.TYPE, Double.class);
            put(Float.TYPE, Float.class);
        }

        @Override
        public Class<?> get(Object key) {
            Class<?> nonNull = super.get(key);
            if (nonNull == null) {
                throw new IllegalArgumentException();
            }
            return nonNull;
        }
    });

    private Class<?> convertPrimitives(Class<?> parameterType) {
        if (!parameterType.isPrimitive()) {
            return parameterType;
        }

        return primitiveToWrapper.get(parameterType);
    }

    private Method getFactory(Class<?> type) {

        Iterable<Map.Entry<String, Class<?>>> possibleFactories = Arrays.<Map.Entry<String, Class<?>>>asList(
                new AbstractMap.SimpleEntry<String, Class<?>>("valueOf", String.class),
                new AbstractMap.SimpleEntry<String, Class<?>>("valueOf", Object.class),
                new AbstractMap.SimpleEntry<String, Class<?>>("parse", String.class)
        );


        for (Map.Entry<String, Class<?>> possibleFactory : possibleFactories) {
            Method factoryMethod = getFactoryMethod(type, possibleFactory.getKey(), possibleFactory.getValue());
            if(factoryMethod != null){
                return factoryMethod;
            }
        }
        throw new IllegalArgumentException();
    }

    private Method getFactoryMethod(Class<?> parameterType, String key, Class<?> value) {
        try {
            return parameterType.getMethod(key, value);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }


    private String[] gerParameterValues(String parameterName) {
        String[] values = parameters.get(parameterName);
        return values != null ? values : new String[0];
    }
}
