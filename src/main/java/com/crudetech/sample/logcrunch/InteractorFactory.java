package com.crudetech.sample.logcrunch;

public interface InteractorFactory<TInteractor> {
    TInteractor createInteractor();
}
