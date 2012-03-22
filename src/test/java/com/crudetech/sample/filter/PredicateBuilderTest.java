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
        assertThat(true || true && false, is(true));
        assertThat((true || true) && false, is(false));

        Predicate<Integer> pred = builder.start(isTrue).or(isTrue).and(isFalse).build();

        assertThat(pred.evaluate(AnyInt), is(true));
    }
    // (x || y) && z
    // builder.openBrace(x).or(y).closeBrace.and(z).build()

    // builder.and(isTrue).or(isFalse).andOpenBraces().add(isTrue).or(isFalse).closeBraces().orOpenBraces()

}
