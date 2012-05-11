package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.filter.BinaryFunction;
import com.crudetech.sample.filter.FilterIterable;
import com.crudetech.sample.filter.Predicate;

import java.lang.reflect.Method;
import java.util.*;

import static com.crudetech.sample.filter.Algorithm.accumulate;
import static java.util.Arrays.asList;

public class ParameterMapper {
    private final Map<String, String[]> parameters;

    private final Map<Class<?>, ParameterFactory> factories = new ClassHierarchyMap<ParameterFactory>(){{
        put(Number.class, new ReflectionParameterFactory("valueOf", String.class));
        put(String.class, new ReflectionParameterFactory("valueOf", Object.class));
        put(Enum.class, new ReflectionParameterFactory("valueOf", String.class));
    }};

    static class ClassHierarchyMap<T> extends AbstractMap<Class<?>, T> {
        private final List<Entry<Class<?>, T>> entrySet = new ArrayList<Entry<Class<?>, T>>();

        @Override
        public Set<Entry<Class<?>, T>> entrySet() {
            return new AbstractSet<Entry<Class<?>, T>>() {
                @Override
                public Iterator<Entry<Class<?>, T>> iterator() {
                    return entrySet.iterator();
                }

                @Override
                public int size() {
                    return entrySet.size();
                }
            };
        }

        @Override
        public T put(Class<?> key, T value) {
            if(!containsKey(key)){
                entrySet.add(new SimpleImmutableEntry<Class<?>, T>(key, value));
                return value;
            }
            return null;
        }

        @Override
        public T get(Object key) {
            for (Entry<Class<?>, T> entry : entrySet) {
                if(entry.getKey().isAssignableFrom((Class<?>) key)) {
                    return entry.getValue();
                }
            }
            throw new IllegalArgumentException("Could not find entry for key type " + key);
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

    public ParameterMapper(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    public void registerParameterFactory(Class<?> type, ParameterFactory factory) {
        factories.put(type, factory);
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

        Class<?> parameterType = getParameterType(annotatedMethod);
        ParameterFactory factory = factories.get(parameterType);

        ParameterConsumer parameterTarget = new ParameterConsumer(mappingTarget, annotatedMethod);

        accumulate(parameterTarget, parameterValues, applyParameter(parameterType, factory)).validate();
    }

    private BinaryFunction<ParameterConsumer, ParameterConsumer, String> applyParameter(final Class<?> parameterType, final ParameterFactory factory) {
        return new BinaryFunction<ParameterConsumer, ParameterConsumer, String>() {
            @Override
            public ParameterConsumer evaluate(ParameterConsumer parameterTarget, String parameterValue) {
                Object value = factory.create(parameterType, parameterValue);
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


    public interface ParameterFactory {
        Object create(Class<?> parameterType, String parameterValue);
    }

    public static class ReflectionParameterFactory implements ParameterFactory {
        private final String factoryMethodName;
        private final Class<?> factoryParameterType;

        public ReflectionParameterFactory(String factoryMethodName, Class<?> factoryParameterType) {
            this.factoryMethodName = factoryMethodName;
            this.factoryParameterType = factoryParameterType;
        }

        @Override
        public Object create(Class<?> parameterType, String parameterValue) {
            Method factoryMethod = getFactoryMethod(parameterType);
            try {
                return factoryMethod.invoke(null, parameterValue);
            } catch (Exception e) {
                throw new BadFormatException(e);
            }
        }

        private Method getFactoryMethod(Class<?> type) {
            return getFactoryMethod(type, factoryMethodName, factoryParameterType);
        }

        private Method getFactoryMethod(Class<?> factoryType, String factoryMethodName, Class<?> factoryMethodParameter) {
            try {
                return factoryType.getMethod(factoryMethodName, factoryMethodParameter);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e.getMessage(), e);
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

        @SuppressWarnings("UnusedReturnValue")
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
