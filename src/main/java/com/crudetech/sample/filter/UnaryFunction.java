package com.crudetech.sample.filter;

public interface UnaryFunction<TResult, TArg> {
    TResult evaluate(TArg argument);
}
