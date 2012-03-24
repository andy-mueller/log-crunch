package com.crudetech.sample.filter;


import java.util.ArrayList;
import java.util.Iterator;

public class MappingIterable<TFrom, TTo> implements Iterable<TTo>{
    private final Iterable<TFrom> source;
    private final UnaryFunction<TTo, TFrom> select;

    public MappingIterable(Iterable<TFrom> source, UnaryFunction<TTo, TFrom> select) {
        if(source == null || select == null){
            throw new IllegalArgumentException();
        }
        this.source = source;
        this.select = select;
    }

    @Override
    public Iterator<TTo> iterator() {
        return new Iterator<TTo>() {
            private final Iterator<TFrom> wrapped = source.iterator();
            @Override
            public boolean hasNext() {
                return wrapped.hasNext();
            }

            @Override
            public TTo next() {
                return select.evaluate(wrapped.next());
            }

            @Override
            public void remove() {
                wrapped.remove();
            }
        };
    }

    @Override
    public String toString() {
        ArrayList<TTo> vals = new ArrayList<TTo>();
        for(TTo t : this){
            vals.add(t);
        }
        return vals.toString();
    }
}
