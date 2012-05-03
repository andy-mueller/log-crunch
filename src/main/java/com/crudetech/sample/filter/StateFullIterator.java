package com.crudetech.sample.filter;

import java.util.NoSuchElementException;

public abstract class StateFullIterator<T> {
    private Cursor<T> cursor;

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

        protected Cursor(T current, boolean hasNext) {
            this.hasNext = hasNext;
            this.current = current;
        }

        public  static <T> Cursor<T> on(T value) {
            return new Cursor<T>(value, true);
        }

        public static <T> Cursor<T> end() {
            return new Cursor<T>(null, false);
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
