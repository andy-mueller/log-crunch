package com.crudetech.sample.filter;


import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;



public class StateFullIteratorTest {
    static class StateFullForwardingIterator extends StateFullIterator<Integer>{
        private final Iterator<Integer> inner;

        StateFullForwardingIterator(Iterator<Integer> inner) {
            this.inner = inner;
        }

        @Override
        protected Cursor incrementCursor() {
            if(inner.hasNext()){
                return new Cursor(inner.next(), true);
            }
            return new Cursor(null, false);
        }
    }
    @Test
    public void ctor(){
        StateFullIterator<Integer> i = new StateFullForwardingIterator(asList(1,2,3).iterator());
        List<Integer> actual = new ArrayList<Integer>();
        while(i.hasNext()){
            actual.add(i.next());
        }

        assertThat(actual, is(asList(1,2,3)));
    }
}
