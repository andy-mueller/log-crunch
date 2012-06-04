package com.crudetech.sample.logcrunch.http;

public interface LogFileInteractor<TQuery, TResult> {
    void interact(TQuery q, TResult r);
}
