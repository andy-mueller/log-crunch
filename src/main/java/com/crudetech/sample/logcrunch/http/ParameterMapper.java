package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.filter.FilterIterable;
import com.crudetech.sample.filter.Predicate;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

public class ParameterMapper {
    private final Map<String, String[]> parameters;

    public ParameterMapper(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    public class BadFormatException extends RuntimeException {
        private BadFormatException(Throwable e) {
            super(e);
        }
    }

    public class NoParameterException extends RuntimeException {
    }

    public void mapTo(Object mappingTarget) {
        for (Method method : allAnnotatedMethods(mappingTarget)) {
            mapOnAnnotatedMethod(mappingTarget, method);
        }
    }

    private static Iterable<Method> allAnnotatedMethods(Object o) {
        return new FilterIterable<Method>(asList(o.getClass().getMethods()), new Predicate<Method>() {
            @Override
            public Boolean evaluate(Method argument) {
                return argument.isAnnotationPresent(Parameter.class);
            }
        });
    }

    private void mapOnAnnotatedMethod(Object mappingTarget, Method annotatedMethod) {
        Parameter parameterAnnotation = annotatedMethod.getAnnotation(Parameter.class);
        Class<?> parameterType = getParameterType(annotatedMethod);
        Method factory = getFactoryMethod(parameterType);
        String parameterName = parameterAnnotation.value();
        String[] parameterValues = gerParameterValues(parameterName);

        if (parameterValues.length == 0 && parameterAnnotation.required()) {
            throw new NoParameterException();
        }
        for (String parameterValue : parameterValues) {
            Object parameter = newInstance(factory, parameterValue);
            invoke(annotatedMethod, mappingTarget, parameter);
        }
    }

    private Object invoke(Method method, Object o, Object parameter) {
        method.setAccessible(true);
        try {
            return method.invoke(o, parameter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object newInstance(Method factoryMethod, String parameter) {
        try {
            return invoke(factoryMethod, null, parameter);
        } catch (Exception e) {
            throw new BadFormatException(e);
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

    @SuppressWarnings("unchecked")
    private static final Iterable<Map.Entry<String, Class<?>>> possibleFactories = Arrays.<Map.Entry<String, Class<?>>>asList(
            new AbstractMap.SimpleEntry<String, Class<?>>("valueOf", String.class),
            new AbstractMap.SimpleEntry<String, Class<?>>("valueOf", Object.class),
            new AbstractMap.SimpleEntry<String, Class<?>>("parse", String.class)
    );

    private Method getFactoryMethod(Class<?> type) {
        for (Map.Entry<String, Class<?>> possibleFactory : possibleFactories) {
            Method factoryMethod = getFactoryMethod(type, possibleFactory.getKey(), possibleFactory.getValue());
            if (factoryMethod != null) {
                return factoryMethod;
            }
        }
        throw new IllegalArgumentException();
    }

    private Method getFactoryMethod(Class<?> factoryType, String factoryMethodName, Class<?> factoryMethodParameter) {
        try {
            return factoryType.getMethod(factoryMethodName, factoryMethodParameter);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }


    private String[] gerParameterValues(String parameterName) {
        String[] values = parameters.get(parameterName);
        return values != null ? values : new String[0];
    }
}
