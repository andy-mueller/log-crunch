package com.crudetech.sample.filter;


import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.crudetech.sample.Iterables.copy;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MappingIterableTest {

    private UnaryFunction<String, Integer> intToString;

    @Before
    public void setUp() throws Exception {
        intToString = new UnaryFunction<String, Integer>() {
            @Override
            public String evaluate(Integer argument) {
                return argument.toString();
            }
        };
    }

    @Test
    public void IteratorMapsInnerUsingFunction() {

        Iterable<Integer> src = asList(0, 1, 2, 3);

         Iterable<String> mapped = new MappingIterable<Integer, String>(src, intToString);

        List<String> result = copy(mapped);
        assertThat(result, is(asList("0", "1", "2", "3")));
    }

    @Test
    public void emptySource() {
        Iterable<Integer> src = emptyList();
        Iterable<String> mapped = new MappingIterable<Integer, String>(src, intToString);

        List<String> result = copy(mapped);
        List<String> expected = emptyList();
        assertThat(result, is(expected));
    }
}
