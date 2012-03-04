package com.crudetech.sample.filter;

public interface UnaryFunction<TArg, TResult> {
    TResult evaluate(TArg argument);
}
