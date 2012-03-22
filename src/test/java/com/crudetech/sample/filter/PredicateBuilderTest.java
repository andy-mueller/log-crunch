package com.crudetech.sample.filter;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class PredicateBuilderTest {
    @Test
    public void creation(){
        PredicateBuilder<Integer> builder =  PredicateBuilder.forClass(Integer.class);
        assertThat(builder, is(notNullValue()));

        builder.and(isTrue).or(isFalse).andOpenBraces().add(isTrue).or(isFalse).closeBraces().orOpenBraces()
    }

    @Test
    public void a(){
        PredicateBuilder<Integer> builder =  PredicateBuilder.forClass(Integer.class);
        assertThat(builder, is(notNullValue()));
    }
}
