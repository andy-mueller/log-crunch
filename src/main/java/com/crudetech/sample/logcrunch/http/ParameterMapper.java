package com.crudetech.sample.logcrunch.http;

import java.lang.reflect.Method;
import java.util.Map;

public class ParameterMapper {
    private final Map<String, String[]> parameters;

    public ParameterMapper(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    public void mapTo(Object o){
        Method[] methods = o.getClass().getMethods();
        for (Method method : methods) {
            Parameter par = method.getAnnotation(Parameter.class);
            if(par == null){
                continue;
            }

            Class<?> parameterType = getParameterType(method);
//            parameterType.isPrimitive()
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

    private Object newInstance(Method factory, String parameterValue)  {
        try {
            return setValue(factory, null, parameterValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> getParameterType(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if(parameterTypes.length != 1){
            throw new RuntimeException();
        }
        return parameterTypes[0];
    }

    private Method getFactory(Class<?> parameterType) {
        try {
            return parameterType.getMethod("valueOf", new Class<?>[]{String.class});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] gerParameterValues(String parameterName) {
        String[] values = parameters.get(parameterName);
        return values != null ? values : new String[0];
    }
}
