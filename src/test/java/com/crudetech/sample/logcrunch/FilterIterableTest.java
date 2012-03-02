package com.crudetech.sample.logcrunch;

import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static com.crudetech.sample.logcrunch.IntegerPredicates.isEven;
import static com.crudetech.sample.logcrunch.IntegerPredicates.isOdd;
import static com.crudetech.sample.logcrunch.Iterables.copy;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FilterIterableTest {
    @Test
    public void hasNextIsTrueOnFirstItemTrue() {
        Iterator<Integer> source = createSourceIterator(1, 2);
        FilterIterator<Integer> it = new FilterIterator<Integer>(source, isOdd());

        assertThat(it.hasNext(), is(true));
    }

    @Test
    public void hasNextWorksOnFirstItemFalse() {
        Iterator<Integer> source = createSourceIterator(1);
        FilterIterator<Integer> it = new FilterIterator<Integer>(source, isEven());

        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void hasNextIsFalseOnEmptySourceRange() {
        Iterator<Integer> source = createSourceIterator();
        FilterIterator<Integer> it = new FilterIterator<Integer>(source, isEven());

        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void hasNextIsFalseOnNonMatchingSourceRange() {
        Iterator<Integer> source = createSourceIterator(3, 5, 7);
        FilterIterator<Integer> it = new FilterIterator<Integer>(source, isEven());

        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void justNext() {
        Iterator<Integer> source = createSourceIterator(2, 3, 4);
        FilterIterator<Integer> it = new FilterIterator<Integer>(source, isEven());

        assertThat(it.next(), is(2));
        assertThat(it.next(), is(4));
        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void hasNextIsTrueWhenOnlyNextIsFalse() {
        Iterator<Integer> source = createSourceIterator(1, 2);
        FilterIterator<Integer> it = new FilterIterator<Integer>(source, isEven());

        assertThat(it.hasNext(), is(true));
    }

    @Test
    public void nextGivesItemWhenHasNextIsCalled() {
        Iterator<Integer> source = createSourceIterator(1, 2);
        FilterIterator<Integer> it = new FilterIterator<Integer>(source, isEven());

        it.hasNext();
        assertThat(it.next(), is(2));
    }

    @Test
    public void noMatchesGiveEmptyIterator() {
        Iterator<Integer> source = createSourceIterator(1, 3, 5);
        FilterIterator<Integer> it = new FilterIterator<Integer>(source, isEven());

        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void nextGivesItemDirectly() {
        Iterator<Integer> source = createSourceIterator(1, 2);
        FilterIterator<Integer> it = new FilterIterator<Integer>(source, isEven());

        assertThat(it.next(), is(2));
    }

    @Test
    public void filterRange() {
        Iterator<Integer> source = createSourceIterator(1, 2, 3, 4, 5, 6);
        FilterIterator<Integer> it = new FilterIterator<Integer>(source, isOdd());

        List<Integer> actual = copy(it);
        List<Integer> expected = asList(1, 3, 5);
        assertThat(actual, is(expected));
    }



    @SuppressWarnings("unchecked")
    private <T> Iterator<T> createSourceIterator() {
        return (Iterator<T>) emptyList().iterator();
    }

    private <T> Iterator<T> createSourceIterator(T... items) {
        return asList(items).iterator();
    }


}
