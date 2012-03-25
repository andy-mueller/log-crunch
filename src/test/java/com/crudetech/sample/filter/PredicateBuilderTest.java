package com.crudetech.sample.filter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class PredicateBuilderTest {
    private static final Integer AnyInt = 42;
    private PredicateBuilder<Integer> builder;

    @Before
    public void setUp() throws Exception {
        builder = new PredicateBuilder<Integer>();
    }

    @Test
    public void creation() {
        assertThat(builder, is(notNullValue()));
    }

    private static final Predicate<Integer> isTrue = new Predicate<Integer>() {
        @Override
        public Boolean evaluate(Integer unused) {
            return true;
        }

        @Override
        public String toString() {
            return "isTrue";
        }
    };
    private static final Predicate<Integer> isFalse = new Predicate<Integer>() {
        @Override
        public Boolean evaluate(Integer unused) {
            return false;
        }

        @Override
        public String toString() {
            return "isFalse";
        }
    };

    @Test
    public void onePredicate() {
        Predicate<Integer> pred = builder.start(isFalse).build();
        assertThat(pred.evaluate(AnyInt), is(false));
    }

    @Test
    public void emptyBuilderThows() {
        expectedException.expect(IllegalStateException.class);
        builder.build();
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
        Predicate<Integer> pred = builder.start(isFalse).or(isTrue).or(isTrue).and(isFalse).build();
        assertThat("false || true || true && false == true vs. false || (true || true) && false == false", pred.evaluate(AnyInt), is(true));
    }

    @Test
    public void booleanBracesPrecedence() {
        Predicate<Integer> pred2 = builder.start(isFalse).orOpenBrace(isTrue).or(isTrue).closeBrace().and(isFalse).build();
        assertThat("false || (true || true) && false==false", pred2.evaluate(AnyInt), is(false));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void closingBracesWithoutOpeningThrows() {
        expectedException.expect(IllegalStateException.class);
        builder.start(isFalse).closeBrace();
    }

    @Test
    public void multipleBraces() {
        Predicate<Integer> pred = builder.start(isFalse)
                .orOpenBrace(isTrue)
                .orOpenBrace(isTrue).or(isFalse)
                .closeBrace()
                .closeBrace().and(isFalse).build();

        assertThat("false || (true || (true || false)) && false == false", pred.evaluate(AnyInt), is(false));
    }

    @Test
    public void closingStackedBracesWithoutOpeningThrows() {
        expectedException.expect(IllegalStateException.class);
        builder.start(isFalse)
                .orOpenBrace(isTrue)
                .orOpenBrace(isTrue).or(isFalse)
                .closeBrace()
                .closeBrace().and(isFalse).closeBrace().build();
    }

    @Test
    public void canStartWithBrace() {
        Predicate<Integer> predicate = builder.openBrace(isTrue).or(isTrue).closeBrace().and(isFalse).build();
        assertThat("(true || true) && false == false, true || true && false == true", predicate.evaluate(AnyInt), is(false));
    }

    @Test
    public void openBraceThrowsWhenNotUsedAsFirstStatement() {
        expectedException.expect(IllegalStateException.class);
        builder.start(isTrue).openBrace(isTrue);
    }

    @Test
    public void notClosingBraceThrows() {
        expectedException.expect(IllegalStateException.class);
        builder.openBrace(isTrue).or(isTrue).build();
    }
    @Test
    public void andOpenBrace() {
        Predicate<Integer> pred = builder.start(isTrue).andOpenBrace(isTrue).or(isFalse).closeBrace().build();
        assertThat(pred.evaluate(AnyInt), is(true));
    }
    @Test
    public void builderCanBeReused() {
        assertThat(builder.start(isTrue).build().evaluate(AnyInt), is(true));

        Predicate<Integer> pred = builder.start(isFalse).build();
        assertThat(pred.evaluate(AnyInt), is(false));
    }

    @Test
    public void orOpenBraceIgnoresOrWhenCalledAsFirstStatement() {
        Predicate<Integer> pred2 = builder.orOpenBrace(isTrue).or(isTrue).closeBrace().and(isFalse).build();
        assertThat("|| (true || true) && false==false", pred2.evaluate(AnyInt), is(false));
    }
    @Test
    public void andOpenBraceIgnoresOrWhenCalledAsFirstStatement() {
        Predicate<Integer> pred2 = builder.andOpenBrace(isTrue).or(isTrue).closeBrace().and(isFalse).build();
        assertThat("&& (true || true) && false==false", pred2.evaluate(AnyInt), is(false));
    }
}
