package com.crudetech.sample.filter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.crudetech.sample.Iterables.copy;
import static com.crudetech.sample.filter.IntegerPredicates.isEven;
import static com.crudetech.sample.filter.IntegerPredicates.isNegative;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FilterChainTest {
    @Test
    public void multipleFiltersAreCombined() {
        Iterable<Integer> source = asList(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5);
        FilterChain<Integer> chain = new FilterChain<Integer>();
        chain.filterBuilder().start(isNegative()).and(isEven());

        Iterable<Integer> even = chain.apply(source);
        assertThat(copy(even), is(asList(-4, -2)));
    }

    private Predicate<String> isValue(final String s) {
        return new Predicate<String>() {
            @Override
            public Boolean evaluate(String argument) {
                return s.equals(argument);
            }

            @Override
            public String toString() {
                return "is-"+ s;
            }
        };
    }

    @Test
    public void orFilterChain() {
        Iterable<String> source = asList("INFO", "WARN", "INFO", "OTHER", "OTHER", "OTHER");

        FilterChain<String> chain = new FilterChain<String>();
        chain.filterBuilder().start(isValue("INFO")).or(isValue("WARN")).build();

        Iterable<String> even = chain.apply(source);
        assertThat(copy(even), is(asList("INFO", "WARN", "INFO")));
    }
    @Test
    public void orFilterChainWorksWithoutCallingBuild() {
        Iterable<String> source = asList("INFO", "WARN", "INFO", "OTHER", "OTHER", "OTHER");

        FilterChain<String> chain = new FilterChain<String>();
        chain.filterBuilder().start(isValue("INFO")).or(isValue("WARN"));

        Iterable<String> even = chain.apply(source);
        assertThat(copy(even), is(asList("INFO", "WARN", "INFO")));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void applyMustHaveFiltersSet() {
        Iterable<String> source = asList("INFO", "WARN", "INFO", "OTHER", "OTHER", "OTHER");

        FilterChain<String> chain = new FilterChain<String>();

        expectedException.expect(IllegalStateException.class);
        chain.apply(source);
    }
}
