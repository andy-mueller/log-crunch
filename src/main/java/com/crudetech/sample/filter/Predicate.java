package com.crudetech.sample.filter;

public interface Predicate<T> {
    boolean evaluate(T item);
}
