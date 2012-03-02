package com.crudetech.sample.logcrunch;

import org.junit.Test;

import static com.crudetech.sample.logcrunch.IntegerPredicates.isEven;
import static com.crudetech.sample.logcrunch.IntegerPredicates.isNegative;
import static com.crudetech.sample.logcrunch.Iterables.copy;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FilterChainTest {

    @Test
    public void singleFilterIsApplied() {
        Iterable<Integer> source = asList(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5);

        FilterChain<Integer> chain = new FilterChain<Integer>(asList(isEven()));

        Iterable<Integer> even = chain.apply(source);
        assertThat(copy(even), is(asList(-4,  -2, 0,  2,  4)));
    }


    @Test
    public void multipleFiltersAreCombined() {
        Iterable<Integer> source = asList(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5);

        FilterChain<Integer> chain = new FilterChain<Integer>(asList(isEven(), isNegative()));

        Iterable<Integer> even = chain.apply(source);
        assertThat(copy(even), is(asList(-4,  -2)));
    }
}
