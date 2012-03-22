package com.crudetech.sample.filter;

import java.util.ArrayList;
import java.util.List;

public  abstract class PredicateBuilder<T> {

    public static <T> PredicateBuilder<T> forClass(Class<T> clazz) {
        return new StackedPredicateBuilder<T>();
    }

    static class StackedPredicateBuilder<T> extends PredicateBuilder<T>{
        private final List<Predicate<T>> predicates = new ArrayList<Predicate<T>>();
        public PredicateBuilder<T> start(Predicate<? super T> predicate) {
            this.predicates.add(super_cast(predicate));
            return this;
        }
        private Predicate<T> super_cast(final Predicate<? super T> predicate) {
            return new Predicate<T>() {
                @Override
                public Boolean evaluate(T argument) {
                    return predicate.evaluate(argument);
                }
            };
        }
        public PredicateBuilder<T> or(Predicate<? super T> predicate) {
            this.predicates.add(super_cast(predicate));
            return this;
        }
        public PredicateBuilder<T> and(Predicate<? super T> predicate) {
            Predicate<T> and = Predicates.<T>and(getLastPredicate(), super_cast(predicate));
            this.predicates.set(this.predicates.size() - 1, and);
            return this;
        }
        private Predicate<? super T> getLastPredicate() {
            return this.predicates.get(this.predicates.size() - 1);
        }
        public Predicate<T> build() {
            return Predicates.or(getOrPredicates());
        }
        private Iterable<Predicate<? super T>> getOrPredicates() {
            return new MappingIterable<Predicate<T>, Predicate<? super T>>(predicates, addSuper());
        }

        private UnaryFunction<Predicate<? super T>, Predicate<T>> addSuper() {
            return new UnaryFunction<Predicate<? super T>, Predicate<T>>() {
                @Override
                public Predicate<? super T> evaluate(Predicate<T> argument) {
                    return argument;
                }
            };
        }
    }

    public abstract PredicateBuilder<T> start(Predicate<? super T> predicate);

    public abstract PredicateBuilder<T> or(Predicate<? super T> predicate);

    public abstract PredicateBuilder<T> and(Predicate<? super T> predicate);

    public abstract Predicate<T> build();
}
