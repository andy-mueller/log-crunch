package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.BinaryFunction;
import com.crudetech.sample.filter.FilterIterable;
import com.crudetech.sample.filter.Predicate;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.crudetech.sample.filter.Algorithm.accumulate;
import static java.util.Arrays.asList;

public class ParameterMapper {
    private final Map<String, String[]> parameters;

    public ParameterMapper(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    public static class BadFormatException extends RuntimeException {
        private BadFormatException(Throwable e) {
            super(e);
        }
    }

    public static class NoParameterException extends RuntimeException {
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
        Iterable<String> parameterValues = getParameterValuesForMethod(annotatedMethod);

        ParameterFactory factory = new ParameterFactory(annotatedMethod);
        ParameterConsumer parameterTarget = new ParameterConsumer(mappingTarget, annotatedMethod);

        accumulate(parameterTarget, parameterValues, applyParameter(factory)).validate();
    }

    private BinaryFunction<ParameterConsumer, ParameterConsumer, String> applyParameter(final ParameterFactory factory) {
        return new BinaryFunction<ParameterConsumer, ParameterConsumer, String>() {
            @Override
            public ParameterConsumer evaluate(ParameterConsumer parameterTarget, String parameterValue) {
                Object value = factory.create(parameterValue);
                parameterTarget.setParameter(value);
                return parameterTarget;
            }
        };
    }

    private Iterable<String> getParameterValuesForMethod(Method annotatedMethod) {
        Parameter parameterAnnotation = annotatedMethod.getAnnotation(Parameter.class);
        String[] values = parameters.get(parameterAnnotation.value());
        return asList(values != null ? values : new String[0]);
    }

    private static class ParameterFactory {
        private final Method factoryMethod;

        @SuppressWarnings("unchecked")
        private static final Iterable<Map.Entry<String, Class<?>>> possibleFactories = Arrays.<Map.Entry<String, Class<?>>>asList(
                new AbstractMap.SimpleEntry<String, Class<?>>("valueOf", String.class),
                new AbstractMap.SimpleEntry<String, Class<?>>("valueOf", Object.class),
                new AbstractMap.SimpleEntry<String, Class<?>>("parse", String.class)
        );

        ParameterFactory(Method annotatedMethod) {
            factoryMethod = getFactoryMethod(getParameterType(annotatedMethod));
        }

        private Method getFactoryMethod(Class<?> type) {
            for (Map.Entry<String, Class<?>> possibleFactory : possibleFactories) {
                Method factoryMethod = getFactoryMethodIfPresent(type, possibleFactory.getKey(), possibleFactory.getValue());
                if (factoryMethod != null) {
                    return factoryMethod;
                }
            }
            throw new IllegalArgumentException("No factory method on type " + type);
        }

        private Method getFactoryMethodIfPresent(Class<?> factoryType, String factoryMethodName, Class<?> factoryMethodParameter) {
            try {
                return factoryType.getMethod(factoryMethodName, factoryMethodParameter);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        private Class<?> getParameterType(Method method) {
            return convertPrimitives(method.getParameterTypes()[0]);
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

        Object create(String parameterValue) {
            try {
                return factoryMethod.invoke(null, parameterValue);
            } catch (Exception e) {
                throw new BadFormatException(e);
            }
        }
    }

    private static class ParameterConsumer {
        private final Object mappingTarget;
        private final Method annotatedMethod;
        private int called = 0;

        ParameterConsumer(Object mappingTarget, Method annotatedMethod) {
            this.mappingTarget = mappingTarget;
            this.annotatedMethod = annotatedMethod;
        }

        void setParameter(Object parameter) {
            invokeMethod(annotatedMethod, mappingTarget, parameter);
            ++called;
        }

        private static Object invokeMethod(Method method, Object o, Object parameter) {
            method.setAccessible(true);
            try {
                return method.invoke(o, parameter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        public void validate() {
            Parameter paramAnnotation = annotatedMethod.getAnnotation(Parameter.class);
            if (called == 0 && paramAnnotation.required()) {
                throw new NoParameterException();
            }
        }
    }
}
