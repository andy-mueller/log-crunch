package com.crudetech.sample.logcrunch;

public interface Predicate<T> {
    boolean evaluate(T item);
}
