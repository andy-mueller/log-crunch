package com.crudetech.sample.filter;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class PredicateBuilderTest {
    private static final Integer AnyInt = 42;
    private PredicateBuilder<Integer> builder;

    @Before
    public void setUp() throws Exception {
        builder = PredicateBuilder.forClass(Integer.class);
    }

    @Test
    public void creation() {
        PredicateBuilder<Integer> builder = PredicateBuilder.forClass(Integer.class);
        assertThat(builder, is(notNullValue()));

    }

    private static final Predicate<Integer> isTrue = new Predicate<Integer>() {
        @Override
        public Boolean evaluate(Integer unused) {
            return true;
        }
    };
    private static final Predicate<Integer> isFalse = new Predicate<Integer>() {
        @Override
        public Boolean evaluate(Integer unused) {
            return false;
        }
    };

    @Test
    public void onePredicate() {
        Predicate<Integer> pred = builder.start(isFalse).build();
        assertThat(pred.evaluate(AnyInt), is(false));
    }

    @Test
    public void twoPredicatesWithAnd() {
        Predicate<Integer> pred = builder.start(isFalse).and(isTrue).build();
        assertThat(pred.evaluate(AnyInt), is(false));
    }

    @Test
    public void twoTruePredicatesWithAnd() {
        Predicate<Integer> pred = builder.start(isTrue).and(isTrue).build();
        assertThat(pred.evaluate(AnyInt), is(true));
    }

    @Test
    public void twoPredicatesWithOr() {
        Predicate<Integer> pred = builder.start(isFalse).or(isTrue).build();
        assertThat(pred.evaluate(AnyInt), is(true));
    }

    @Test
    public void booleanOpPrecedence() {
        assertThat(false || true || true && false, is(true));
        assertThat(false || (true || true) && false, is(false));

        Predicate<Integer> pred = builder.start(isFalse).or(isTrue).or(isTrue).and(isFalse).build();

        assertThat(pred.evaluate(AnyInt), is(true));
    }

    @Test
    public void booleanBracesPrecedence() {
        assertThat(false || (true || true) && false, is(false));

        Predicate<Integer> pred2 = builder.start(isFalse).orOpenBrace(isTrue).or(isTrue).closeBrace().and(isFalse).build();
        assertThat(pred2.evaluate(AnyInt), is(false));
    }

    // start 2 times throws
    //closing braces w/o opening throws
    //starting brace right away works (builder.openBrace)
    //openBrace not at start throws
    //not closing brace throws
    // stacked braces
    //andOpenBrace

    // Builder ->StackTail->next->next->(first)builder
}
