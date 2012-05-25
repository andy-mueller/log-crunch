package com.crudetech.sample.filter;


import com.crudetech.sample.Iterables;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;


public class CursorIteratorTest {
    static class CursorForwardingIterator extends CursorIterator<Integer> {
        private final Iterator<Integer> inner;

        CursorForwardingIterator(Iterator<Integer> inner) {
            this.inner = inner;
        }

        @Override
        protected Cursor<Integer> incrementCursor() {
            if (inner.hasNext()) {
                return Cursor.on(inner.next());
            }
            return Cursor.end();
        }
    }

    @Test
    public void standardIteration() {
        CursorIterator<Integer> i = new CursorForwardingIterator(asList(1, 2, 3).iterator());
        List<Integer> actual = new ArrayList<Integer>();
        while (i.hasNext()) {
            actual.add(i.next());
        }

        assertThat(actual, is(asList(1, 2, 3)));
    }

    @Test
    public void givenEmptyRangeIteratorIsEmpty() {
        Iterable<Integer> emptyList = emptyList();
        CursorIterator<Integer> i = new CursorForwardingIterator(emptyList.iterator());
        List<Integer> actual = new ArrayList<Integer>();
        while (i.hasNext()) {
            actual.add(i.next());
        }

        assertThat(actual, hasSize(0));
    }

    static abstract class ConditionalConcatIterator<T> extends CursorIterator<T> {
        private final Iterator<T> src;
        private final OptionalNext optionalNext = new OptionalNext();

        ConditionalConcatIterator(Iterator<T> src) {
            this.src = src;
        }

        private class OptionalNext {
            boolean nextStored = false;
            T next = null;

            T pop() {
                T rv = nextStored ? next : src.next();
                nextStored = false;
                return rv;
            }

            void put(T next) {
                nextStored = true;
                this.next = next;
            }

            boolean isStored() {
                return nextStored;
            }

            public boolean isEmpty() {
                return !isStored();
            }
        }


        @Override
        protected Cursor<T> incrementCursor() {
            if (sourceHasFinished()) {
                return Cursor.end();
            }

            T result = optionalNext.pop();

            while (sourceHasNext()) {
                T next = src.next();
                if (mustConcat(result, next)) {
                    result = concat(result, next);
                } else {
                    optionalNext.put(next);
                }
            }
            return Cursor.on(result);
        }

        protected abstract T concat(T result, T next);

        protected abstract boolean mustConcat(T result, T next);

        private boolean sourceHasNext() {
            return src.hasNext() && optionalNext.isEmpty();
        }

        private boolean sourceHasFinished() {
            return !src.hasNext() && optionalNext.isEmpty();
        }
    }

    static class ConditionalStringLineConcatIterator extends ConditionalConcatIterator<String> {
        ConditionalStringLineConcatIterator(Iterator<String> src) {
            super(src);
        }

        @Override
        protected String concat(String result, String next) {
            return result + next;
        }

        @Override
        protected boolean mustConcat(String result, String next) {
            return next.startsWith("X");
        }
    }

    @Test
    public void concatAllLinesWithAnX() {
        Iterable<String> lines = asList(
                "sajlfdhaskjhfska",
                "Xcwnecasinecn",
                "sahdfhauwcn",
                "Xasncnawiecwfo"
        );

        Iterator<String> concatX = new ConditionalStringLineConcatIterator(lines.iterator());

        List<String> actual = Iterables.copy(concatX);
        List<String> expected = asList(
                "sajlfdhaskjhfskaXcwnecasinecn",
                "sahdfhauwcnXasncnawiecwfo"
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void concatAllLinesWithAnXWhenUnevenlyDistributed() {
        Iterable<String> lines = asList(
                "sajlfdhaskjhfska",
                "sahdfhauwcn",
                "Xcwnecasinecn",
                "asncnawiecwfo"
        );

        Iterator<String> concatX = new ConditionalStringLineConcatIterator(lines.iterator());

        List<String> actual = Iterables.copy(concatX);
        List<String> expected = asList(
                "sajlfdhaskjhfska",
                "sahdfhauwcnXcwnecasinecn",
                "asncnawiecwfo"
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void concatAllLinesWithAnXWhenMultipleInARow() {
        Iterable<String> lines = asList(
                "sajlfdhaskjhfska",
                "sahdfhauwcn",
                "Xcwnecasinecn",
                "Xijneawccnne",
                "asncnawiecwfo"
        );

        Iterator<String> concatX = new ConditionalStringLineConcatIterator(lines.iterator());

        List<String> actual = Iterables.copy(concatX);
        List<String> expected = asList(
                "sajlfdhaskjhfska",
                "sahdfhauwcnXcwnecasinecnXijneawccnne",
                "asncnawiecwfo"
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void concatAllLinesWithAnXWhenEndsWithX() {
        Iterable<String> lines = asList(
                "sajlfdhaskjhfska",
                "sahdfhauwcn",
                "Xcwnecasinecn",
                "Xijneawccnne",
                "asncnawiecwfo",
                "Xcwnecasinecn"
        );

        Iterator<String> concatX = new ConditionalStringLineConcatIterator(lines.iterator());

        List<String> actual = Iterables.copy(concatX);
        List<String> expected = asList(
                "sajlfdhaskjhfska",
                "sahdfhauwcnXcwnecasinecnXijneawccnne",
                "asncnawiecwfoXcwnecasinecn"
        );
        assertThat(actual, is(expected));
    }
}
