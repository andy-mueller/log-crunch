package com.crudetech.sample.logcrunch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class FilterChain<T> {
    private final List<Predicate<T>> filters;

    public FilterChain(Collection<Predicate<T>> predicates) {
        filters = new ArrayList<Predicate<T>>(predicates);
    }

    public Iterable<T> apply(Iterable<? extends T> source) {
        FilterIterable<T> filter = new FilterIterable<T>(source, filters.get(0));

        for(int i = 1; i < filters.size(); ++i){
            filter = new FilterIterable<T>(filter, filters.get(i));
        }
                       //test
        return filter;
    }
}
