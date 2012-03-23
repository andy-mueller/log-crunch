package com.crudetech.sample.filter;

import java.util.ArrayList;
import java.util.List;

public abstract class PredicateBuilder<T> {

    public static <T> PredicateBuilder<T> forClass(Class<T> clazz) {
        return new PredicateBuilder<T>() {
        };
    }

    private StackPredicateBuilder head;

    PredicateBuilder() {

    }

    public PredicateBuilder<T> start(Predicate<? super T> predicate) {
        head = new StackPredicateBuilder();
        head.start(predicate);
        return this;
    }

    public PredicateBuilder<T> or(Predicate<? super T> predicate) {
        return this;
    }

    public PredicateBuilder<T> and(Predicate<? super T> predicate) {
        return this;
    }

    public Predicate<T> build() {
        return head.build();
    }

    public PredicateBuilder<T> orOpenBrace(Predicate<? super T> predicate) {
        return this;
    }

    public PredicateBuilder<T> closeBrace() {
        return this;
    }

    public PredicateBuilder<T> openBrace(Predicate<? super T> predicate) {
        return this;
    }

    private class StackPredicateBuilder extends PredicateBuilder {
        private final List<Predicate<T>> predicates = new ArrayList<Predicate<T>>();

        @Override
        public PredicateBuilder<T> start(Predicate<? super T> predicate) {
            predicates.add(super_away(predicate));
            return owner();
        }

        private PredicateBuilder<T> owner() {
            return PredicateBuilder.this;
        }

        private Predicate<T> super_away(final Predicate<? super T> predicate) {
            return new Predicate<T>() {
                @Override
                public Boolean evaluate(T argument) {
                    return predicate.evaluate(argument);
                }
            };
        }

        @Override
        public PredicateBuilder<T> or(Predicate<? super T> predicate) {
            throw new RuntimeException("com.crudetech.sample.filter.PredicateBuilder.StackPredicateBuilder.or is not implemented!");
        }

        @Override
        public PredicateBuilder<T> and(Predicate<? super T> predicate) {
            throw new RuntimeException("com.crudetech.sample.filter.PredicateBuilder.StackPredicateBuilder.and is not implemented!");
        }

        @Override
        public Predicate<T> build() {
            return Predicates.or(getPredicates());
        }

        private Iterable<Predicate<? super T>> getPredicates() {
            return new MappingIterable<Predicate<T>, Predicate<? super T>>(predicates, new UnaryFunction<Predicate<? super T>, Predicate<T>>() {
                @Override
                public Predicate<? super T> evaluate(Predicate<T> argument) {
                    return argument;
                }
            });
        }

        @Override
        public PredicateBuilder<T> orOpenBrace(Predicate<? super T> predicate) {
            throw new RuntimeException("com.crudetech.sample.filter.PredicateBuilder.StackPredicateBuilder.orOpenBrace is not implemented!");
        }

        @Override
        public PredicateBuilder<T> closeBrace() {
            throw new RuntimeException("com.crudetech.sample.filter.PredicateBuilder.StackPredicateBuilder.closeBrace is not implemented!");
        }

        @Override
        public PredicateBuilder<T> openBrace(Predicate<? super T> predicate) {
            throw new RuntimeException("com.crudetech.sample.filter.PredicateBuilder.StackPredicateBuilder.openBrace is not implemented!");
        }
    }
}
