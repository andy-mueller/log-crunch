package com.crudetech.sample.logcrunch;

import java.util.Iterator;

public class FilterIterable<T> implements Iterable<T>{
    private final Iterable<? extends T> source;
    private final Predicate<? super T> predicate;

    public FilterIterable(Iterable<? extends T> source, Predicate<? super T> predicate) {
        this.predicate = predicate;
        this.source = source;
    }

    @Override
    public Iterator<T> iterator() {
        return new FilterIterator<T>(source.iterator(), predicate);
    }
}
