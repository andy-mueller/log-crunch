package com.crudetech.sample.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConcatIterable<T> implements Iterable<T> {
    private final Iterable<? extends Iterable<T>> iterables;

    public ConcatIterable(Iterable<? extends Iterable<T>> iterables) {

        this.iterables = iterables;
    }

    public static <T> Iterable<T> concat(Iterable<? extends Iterable<T>> iterables) {
        return new ConcatIterable<T>(iterables);
    }
    @Override
    public String toString() {
        List<T> items = new ArrayList<T>();
        for (T item : this) {
            items.add(item);
        }
        return "ConcatIterable{" +
                "iterables=" + items +
                '}';
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<? extends Iterable<T>> iterators = iterables.iterator();
            private Iterator<? extends T> current;

            @Override
            public boolean hasNext() {
                while (current == null && iterators.hasNext()) {
                    current = iterators.next().iterator();
                    if (!current.hasNext()) {
                        current = null;
                    } else {
                        return true;
                    }
                }

                return current != null && current.hasNext();
            }

            @Override
            public T next() {
                try {
                    return current.next();
                } finally {
                    if (!current.hasNext()) {
                        current = null;
                    }
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Implement me!");
            }
        };
    }
}
