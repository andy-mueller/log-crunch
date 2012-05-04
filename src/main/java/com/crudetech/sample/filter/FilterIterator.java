package com.crudetech.sample.filter;

import java.util.Iterator;

public class FilterIterator<T> extends CursorIterator<T> {
    private final Iterator<? extends T> source;
    private final Predicate<? super T> predicate;

    public FilterIterator(Iterator<? extends T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    protected Cursor<T> incrementCursor() {
        while(source.hasNext()){
            T current = source.next();
            if(predicate.evaluate(current)){
                return Cursor.on(current);
            }
        }
        return Cursor.end();
    }
}
