package com.crudetech.sample.filter;


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
}
