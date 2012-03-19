package com.crudetech.sample.filter;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class ConcatIterableTest {
    @Test
    public void concatMultipleIterables() {
        Iterable<Integer> i1 = asList(0, 1, 2);
        Iterable<Integer> i2 = asList(3, 4);
        Iterable<Integer> i3 = asList(5, 6);

        Iterable<Integer> concat = ConcatIterable.concat(iterableList(i1, i2, i3));

        assertThat(concat, is(equalTo(asList(0, 1, 2, 3, 4, 5, 6))));
    }

    @SuppressWarnings("unchecked")
    private Iterable<Iterable<Integer>> iterableList(Iterable<Integer> i1, Iterable<Integer> i2, Iterable<Integer> i3) {
        return asList(i1, i2, i3);
    }

    @Test
    public void matcher() {
        assertThat(asList(0, 1, 2), is(equalTo(asList(0, 1, 2))));
        assertThat(asList(0, 1, 2), is(not(equalTo(asList(0, 2, 2)))));
        assertThat(asList(0, 2, 2), is(not(equalTo(asList(0, 1, 2)))));
        assertThat(asList(0, 1, 2), is(not(equalTo(asList(0, 1)))));
        assertThat(asList(0, 1), is(not(equalTo(asList(0, 1, 2)))));
        assertThat(asList(0, 1, 2), is(not(equalTo(Collections.<Integer>emptyList()))));
    }

    private <T> Matcher<Iterable<T>> equalTo(final Iterable<T> iterable) {
        return new TypeSafeMatcher<Iterable<T>>() {
            @Override
            protected boolean matchesSafely(Iterable<T> item) {
                Iterator<?> i1 = item.iterator();
                Iterator<?> i2 = iterable.iterator();
                while (i1.hasNext() && i2.hasNext()) {
                    Object lhs = i1.next();
                    Object rhs = i2.next();
                    if (!equal(lhs, rhs)) {
                        return false;
                    }
                }
                return !i1.hasNext() && !i2.hasNext();
            }

            private boolean equal(Object lhs, Object rhs) {
                return lhs != null ? lhs.equals(rhs) : lhs == rhs;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(iterable);
            }
        };
    }

    @Test
    public void concatWithOneEmptyIterable() {
        Iterable<Integer> i1 = asList(0, 1, 2);
        Iterable<Integer> i2 = emptyList();
        Iterable<Integer> i3 = asList(5, 6);


        Iterable<Integer> concat = new ConcatIterable<Integer>(iterableList(i1, i2, i3));

        assertThat(concat, is(equalTo(asList(0, 1, 2, 5, 6))));
    }

    @Test
    public void concatWithFirstEmptyIterable() {
        Iterable<Integer> i1 = emptyList();
        Iterable<Integer> i2 = asList(3, 4);
        Iterable<Integer> i3 = asList(5, 6);

        Iterable<Integer> concat = new ConcatIterable<Integer>(iterableList(i1, i2, i3));

        assertThat(concat, is(equalTo(asList(3, 4, 5, 6))));
    }

    @Test
    public void concatWithLastEmptyIterable() {
        Iterable<Integer> i1 = asList(0, 1, 2);
        Iterable<Integer> i2 = asList(3, 4);
        Iterable<Integer> i3 = emptyList();

        Iterable<Integer> concat = new ConcatIterable<Integer>(iterableList(i1, i2, i3));

        assertThat(concat, is(equalTo(asList(0, 1, 2, 3, 4))));
    }
}
