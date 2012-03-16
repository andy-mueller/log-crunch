package com.crudetech.sample.filter;

public interface BinaryFunction<TResult, TArg1, TArg2> {
    TResult evaluate(TArg1 argument1, TArg2 argument2);
}
