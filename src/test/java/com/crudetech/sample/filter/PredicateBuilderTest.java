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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Test
    public void startingMultipleTimesThrows() {
        expectedException.expect(IllegalStateException.class);
        builder.start(isFalse).start(isFalse);
    }

    @Test
    public void closingBracesWithoutOpeningThrows() {
        expectedException.expect(IllegalStateException.class);
        builder.start(isFalse).closeBrace();
    }
    @Test
    public void multipleBraces(){
        assertThat(false || (true || (true || false)) && false, is(false));
        Predicate<Integer> pred = builder.start(isFalse)
                                            .orOpenBrace(isTrue)
                                                        .orOpenBrace(isTrue).or(isFalse)
                                                        .closeBrace()
                                             .closeBrace().and(isFalse).build();

        assertThat(pred.evaluate(AnyInt), is(false));
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
        assertThat( (true || true) && false, is(false));
        assertThat( true || true && false, is(true));

        Predicate<Integer> predicate = builder.openBrace(isTrue).or(isTrue).closeBrace().and(isFalse).build();
        assertThat(predicate.evaluate(AnyInt), is(false));
    }

    //starting brace right away works (builder.openBrace)
    //openBrace not at start throws
    //not closing brace throws
    // stacked braces
    //andOpenBrace

    // Builder ->StackTail->next->next->(first)builder
}
