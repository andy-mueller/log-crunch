package com.crudetech.sample.filter;

public class Algorithm {
    public static <TResult, TSource> TResult accumulate(TResult start,
                                                        Iterable<? extends TSource> range,
                                                        BinaryFunction<TResult, ? super TResult, ? super TSource> accumulator) {
        for (TSource item : range) {
            start = accumulator.evaluate(start, item);
        }
        return start;
    }
}
