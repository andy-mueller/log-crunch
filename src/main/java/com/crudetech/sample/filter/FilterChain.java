package com.crudetech.sample.filter;

import java.util.ArrayList;
import java.util.List;

import static com.crudetech.sample.filter.Algorithm.accumulate;


public class FilterChain<T> {
    private final List<Predicate<T>> filters;

    public FilterChain(Iterable<Predicate<T>> predicates) {
        filters = copy(predicates);
    }

    private static <T> List<Predicate<T>> copy(Iterable<Predicate<T>> predicates) {
        List<Predicate<T>> copy = new ArrayList<Predicate<T>>();
        for (Predicate<T> predicate : predicates) {
            copy.add(predicate);
        }
        return copy;
    }

    public Iterable<T> apply(Iterable<? extends T> source) {
//   JDK 8     Iterable<T> filtered = accumulate(cast(source), filters, (Iterable<T> source, Predicate<T> select)=> new FilterIterable<T>(source, select));
        Iterable<T> filtered = accumulate(cast(source), filters, filterIterableBuilder());
        return filtered;
    }

    @SuppressWarnings("unchecked")
    private Iterable<T> cast(Iterable<? extends T> source) {
        return (Iterable<T>) source;
    }

    private BinaryFunction<Iterable<T>, Iterable<T>, Predicate<T>> filterIterableBuilder() {
        return new BinaryFunction<Iterable<T>, Iterable<T>, Predicate<T>>() {
            @Override
            public Iterable<T> evaluate(Iterable<T> source, Predicate<T> select) {
                return new FilterIterable<T>(source, select);
            }
        };
    }
}
