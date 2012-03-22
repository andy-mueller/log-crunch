package com.crudetech.sample.filter;

import java.util.ArrayList;
import java.util.List;


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
        return new FilterIterable<T>(source, orFilter());
//        Iterable<T> filtered = accumulate(cast(source), filters, filterIterableBuilder());
//        return filtered;
    }

    private Predicate<? super T> orFilter() {
        return new Predicate<T>() {
            @Override
            public Boolean evaluate(T argument) {
                for(Predicate<T> pred : filters){
                    if(pred.evaluate(argument)){
                        return true;
                    }
                }
                return false;
            }
        };
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
