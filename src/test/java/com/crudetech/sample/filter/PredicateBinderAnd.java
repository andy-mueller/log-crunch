package com.crudetech.sample.filter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
public class PredicateBinderAnd {
    private static final int AnyInt = 42;
    private final boolean expectedResult;
    private final Predicate<Integer> firstPred;
    private final Predicate<Integer> secondPred;
    private final Predicate<Integer> thirdPred;

    public PredicateBinderAnd(boolean expectedResult, Predicate<Integer> firstPred, Predicate<Integer> thirdPred, Predicate<Integer> secondPred) {
        this.expectedResult = expectedResult;
        this.firstPred = firstPred;
        this.thirdPred = thirdPred;
        this.secondPred = secondPred;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> createParameters(){
        Predicate<Integer> isTrue = new Predicate<Integer>() {
            @Override
            public Boolean evaluate(Integer argument) {
                return true;
            }
        };

        Predicate<Integer> isFalse = new Predicate<Integer>() {
            @Override
            public Boolean evaluate(Integer argument) {
                return false;
            }
        };
        return asList(new Object[][]{
                {true, isTrue, isTrue, isTrue},

                {false, isFalse, isTrue, isTrue},
                {false, isTrue, isFalse, isTrue},
                {false, isTrue, isTrue, isFalse},

                {false, isFalse, isFalse, isTrue},
                {false, isTrue, isFalse, isFalse},
                {false, isFalse, isTrue, isFalse},

                {false, isFalse, isFalse, isFalse},
        });
    }

    @Test
    public void and(){

        Predicate<Integer> or = Predicates.and(firstPred, secondPred, thirdPred);
        
        assertThat(or.evaluate(AnyInt), is(expectedResult));
    }
}
