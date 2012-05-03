package com.crudetech.sample.filter;

import java.util.NoSuchElementException;

public abstract class StateFullIterator<T> {
    private Cursor cursor;
    private boolean isPositioned = false;

    public boolean hasNext() {
        if (cursor == null) {
            cursor = incrementCursor();
        }
        return cursor.hasNext;
    }

    protected abstract Cursor incrementCursor();

    protected class Cursor {
        private final boolean hasNext;
        private final T current;

        protected Cursor(T current, boolean hasNext) {
            this.hasNext = hasNext;
            this.current = current;
        }
    }


    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        T val = cursor.current;
        if (cursor.hasNext) {
            cursor = incrementCursor();
        }
        return val;
    }
}
