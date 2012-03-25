package com.crudetech.sample.filter;

public class FilterChain<T> {
    private final PredicateBuilder<T> filterBuilder = new PredicateBuilder<T>();

    public Iterable<T> apply(Iterable<? extends T> source) {
        Predicate<T> filter = filterBuilder.build();
        return new FilterIterable<T>(source, filter);
    }

    public PredicateBuilder<T> filterBuilder() {
        return filterBuilder;
    }
}
