package com.crudetech.sample.filter;

public class PredicateBuilder<T> {
    public static <T> PredicateBuilder<T> forClass(Class<T> clazz) {
        return new PredicateBuilder<T>();
    }
}
