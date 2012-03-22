package com.crudetech.sample.filter;

import static java.util.Arrays.asList;

public class Predicates {
    public static <T> Predicate<T> or(Predicate<? super T>... preds) {
        return or(asList(preds));
    }

    public static <T> Predicate<T> or(final Iterable<Predicate<? super T>> predicates) {
        return new Predicate<T>() {
            @Override
            public Boolean evaluate(T argument) {
                for (Predicate<? super T> predicate : predicates) {
                    if (predicate.evaluate(argument)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static <T> Predicate<T> and(Predicate<? super T>... predicates) {
        return and(asList(predicates));
    }

    public static <T> Predicate<T> and(final Iterable<Predicate<? super T>> predicates) {
        return new Predicate<T>() {
            @Override
            public Boolean evaluate(T argument) {
                for (Predicate<? super T> predicate : predicates) {
                    if(!predicate.evaluate(argument)){
                        return false;
                    }
                }
                return true;
            }
        };
    }
}
