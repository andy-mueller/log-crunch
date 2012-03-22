package com.crudetech.sample.filter;

import org.junit.Test;

import java.util.List;

import static com.crudetech.sample.Iterables.copy;
import static com.crudetech.sample.filter.IntegerPredicates.isEven;
import static com.crudetech.sample.filter.IntegerPredicates.isNegative;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FilterChainTest {

    @Test
    public void singleFilterIsApplied() {
        Iterable<Integer> source = asList(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5);

        FilterChain<Integer> chain = new FilterChain<Integer>(predicateList(isEven()));

        Iterable<Integer> even = chain.apply(source);
        assertThat(copy(even), is(asList(-4, -2, 0, 2, 4)));
    }


    @Test
    public void multipleFiltersAreCombined() {
        Iterable<Integer> source = asList(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5);
        FilterChain<Integer> chain = new FilterChain<Integer>(predicateList(isNegative(), isEven()));

        Iterable<Integer> even = chain.apply(source);
        assertThat(copy(even), is(asList(-4, -2)));
    }

    @SuppressWarnings("unchecked")
    static List<Predicate<Integer>> predicateList(Predicate<Integer> pred) {
        return asList(pred);
    }

    @SuppressWarnings("unchecked")
    static List<Predicate<Integer>> predicateList(Predicate<Integer> pred1, Predicate<Integer> pred2) {
        return asList(pred1, pred2);
    }

    @Test
    public void xox() {
        Iterable<String> source = asList("INFO", "WARN", "INFO", "OTHER", "OTHER", "OTHER");

        FilterChain<String> chain = new FilterChain<String>(asList(isValue("INFO"), isValue("WARN")));

        Iterable<String> even = chain.apply(source);
        assertThat(copy(even), is(asList("INFO", "WARN", "INFO")));
    }

    @Test
    public void tt() {
        assertThat(false && true, is(false));

        assertThat(isFalse(1) || isTrue(2) && isFalse(3) || isTrue(4), is(false));
    }

    private boolean isTrue(int id) {
        System.out.print("true("+id+") " );
        return true;
    }

    private boolean isFalse(int id) {
        System.out.print("false("+id+") ");
        return false;
    }

    private Predicate<String> isValue(final String s) {
        return new Predicate<String>() {
            @Override
            public Boolean evaluate(String argument) {
                return s.equals(argument);
            }
        };
    }

}
