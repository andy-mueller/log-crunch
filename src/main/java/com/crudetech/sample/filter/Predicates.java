package com.crudetech.sample.filter;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

public class Predicates {
    public static <T> Predicate<T> or(Predicate<? super T>... preds) {
        return or(asList(preds));
    }

    public static <T> Predicate<T> or(final Iterable<Predicate<? super T>> predicates) {
        if(!predicates.iterator().hasNext()){
            throw new IllegalArgumentException("Cannot \"||\" together nothing!!");
        }
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

            @Override
            public String toString() {
                return Predicates.toString(predicates, "||");
            }
        };
    }


    private static String toString(Iterable<?> predicates, String sym){
        String s = "";
        for(Object o : predicates){
            s += o + " "+sym+" ";
        }
        return s.substring(0, s.length() - 4);
    }
    public static <T> Predicate<T> and(Predicate<? super T>... predicates) {
        return and(asList(predicates));
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> and(Predicate<? super T> lhs, Predicate<? super T> rhs) {
        List<Predicate<? super T>> list = Arrays.<Predicate<? super T>>asList(lhs, rhs);
        return and(list);
    }

    public static <T> Predicate<T> and(final Iterable<? extends Predicate<? super T>> predicates) {
        if(!predicates.iterator().hasNext()){
            throw new IllegalArgumentException("Cannot \"&&\" together nothing!!");
        }
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
            @Override
            public String toString() {
                return Predicates.toString(predicates, "&&");
            }
        };
    }
}
