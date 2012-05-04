package com.crudetech.sample.filter;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class CursorIterator<T> implements Iterator<T> {
    private Cursor<T> cursor;

    @Override
    public boolean hasNext() {
        if (cursor == null) {
            cursor = incrementCursor();
        }
        return cursor.hasNext;
    }

    protected abstract Cursor<T> incrementCursor();

    protected static class Cursor<T> {
        private final boolean hasNext;
        private final T current;

        private Cursor(T current, boolean hasNext) {
            this.hasNext = hasNext;
            this.current = current;
        }

        public static <T> Cursor<T> on(T value) {
            return new Cursor<T>(value, true);
        }

        public static <T> Cursor<T> end() {
            return new Cursor<T>(null, false);
        }
    }


    @Override
    public T next() {
        verifyNextElement();
        try {
            return cursor.current;
        } finally {
            cursor = incrementCursor();
        }
    }

    protected void verifyNextElement() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("This operation is not supported!");
    }
}
