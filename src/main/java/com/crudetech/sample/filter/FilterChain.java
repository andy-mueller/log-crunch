package com.crudetech.sample.filter;

import java.util.ArrayList;
import java.util.List;


public class FilterChain<T> {
    private final PredicateBuilder<T> filterBuilder = new PredicateBuilder<T>();
    public FilterChain(Iterable<Predicate<T>> predicates) {
    }

    public FilterChain() {
    }

    private static <T> List<Predicate<T>> copy(Iterable<Predicate<T>> predicates) {
        List<Predicate<T>> copy = new ArrayList<Predicate<T>>();
        for (Predicate<T> predicate : predicates) {
            copy.add(predicate);
        }
        return copy;
    }

    public Iterable<T> apply(Iterable<? extends T> source) {
        Predicate<T> filter = filterBuilder.build();
        return new FilterIterable<T>(source, filter);
    }

    public PredicateBuilder<T> filterBuilder() {
        return filterBuilder;
    }
}
