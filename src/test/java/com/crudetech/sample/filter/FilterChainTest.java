package com.crudetech.sample.filter;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.List;

import static com.crudetech.sample.filter.IntegerPredicates.isEven;
import static com.crudetech.sample.filter.IntegerPredicates.isNegative;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;

public class FilterChainTest {

    @Test
    public void singleFilterIsApplied() {
        Iterable<Integer> source = asList(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5);

        FilterChain<Integer> chain = new FilterChain<Integer>(predicateList(isEven()));

        Iterable<Integer> even = chain.apply(source);
        MatcherAssert.assertThat(Iterables.copy(even), is(asList(-4, -2, 0, 2, 4)));
    }


    @Test
    public void multipleFiltersAreCombined() {
        Iterable<Integer> source = asList(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5);

        FilterChain<Integer> chain = new FilterChain<Integer>(predicateList(isEven(), isNegative()));

        Iterable<Integer> even = chain.apply(source);
        MatcherAssert.assertThat(Iterables.copy(even), is(asList(-4, -2)));
    }

    @SuppressWarnings("unchecked")
    static <T> List<Predicate<T>> predicateList(Predicate<T> pred) {
        return asList(pred);
    }
    @SuppressWarnings("unchecked")
    static <T> List<Predicate<T>> predicateList(Predicate<T> pred1, Predicate<T> pred2) {
        return asList(pred1, pred2);
    }
}
