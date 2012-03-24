package com.crudetech.sample.filter;

import java.util.ArrayList;
import java.util.List;

public class PredicateBuilder<T> {
    private StackPredicateBuilder head;
    private int braceCount = 0;

    public PredicateBuilder<T> start(Predicate<? super T> predicate) {
        if (head != null) {
            throw new IllegalStateException("This builder was already started!");
        }
        head = new StackPredicateBuilderNone();
        head.start(predicate);
        return this;
    }

    public PredicateBuilder<T> or(Predicate<? super T> predicate) {
        getHead().or(predicate);
        return this;
    }

    public PredicateBuilder<T> and(Predicate<? super T> predicate) {
        getHead().and(predicate);
        return this;
    }

    public Predicate<T> build() {
        if(braceCount != 0){
            throw new IllegalStateException("There are still open braces!");
        }
        return getHead().build();
    }

    private PredicateBuilder<T> getHead() {
        if (head == null) {
            throw new IllegalStateException("Head was not set. this builder was not started correctly!");
        }
        return head;
    }

    public PredicateBuilder<T> orOpenBrace(Predicate<? super T> predicate) {
        head = new StackPredicateBuilderOr(head);
        head.start(predicate);
        ++braceCount;
        return this;
    }
    public PredicateBuilder<T> andOpenBrace(Predicate<T> predicate){
        head = new StackPredicateBuilderAnd(head);
        head.start(predicate);
        ++braceCount;
        return this;
    }
    public PredicateBuilder<T> closeBrace() {
        verifyBracesWereOpened();
        --braceCount;
        head.pop();
        return this;
    }

    private void verifyBracesWereOpened() {
        if(braceCount <= 0){
            throw new IllegalStateException("There were no braces opened");
        }
    }

    public PredicateBuilder<T> openBrace(Predicate<? super T> predicate) {
        if (head != null) {
            throw new IllegalStateException("This builder was already started!");
        }
        ++braceCount;
        head = new StackPredicateBuilderNone();
        head.start(predicate);
        return this;
    }


    private abstract class StackPredicateBuilder extends PredicateBuilder<T> {
        final List<Predicate<T>> predicates = new ArrayList<Predicate<T>>();
        final StackPredicateBuilder previous;

        StackPredicateBuilder(StackPredicateBuilder previous) {
            this.previous = previous;
        }

        @Override
        public PredicateBuilder<T> start(Predicate<? super T> predicate) {
            predicates.add(super_away(predicate));
            return owner();
        }

        PredicateBuilder<T> owner() {
            return PredicateBuilder.this;
        }

        private Predicate<T> super_away(final Predicate<? super T> predicate) {
            return new Predicate<T>() {
                @Override
                public Boolean evaluate(T argument) {
                    return predicate.evaluate(argument);
                }
                @Override
                public String toString(){
                    return predicate.toString();
                }
            };
        }

        @Override
        public PredicateBuilder<T> or(Predicate<? super T> predicate) {
            predicates.add(super_away(predicate));
            return owner();
        }

        @Override
        public PredicateBuilder<T> and(Predicate<? super T> predicate) {
            Predicate<T> and = Predicates.and(lastPredicate(), predicate);
            replaceLastPredicate(and);
            return owner();
        }

        private void replaceLastPredicate(Predicate<T> predicate) {
            predicates.set(predicates.size() - 1, predicate);
        }

        private Predicate<? super T> lastPredicate() {
            return predicates.get(predicates.size() - 1);
        }

        @Override
        public Predicate<T> build() {
            return toStringDecorator(Predicates.or(getPredicates()));
        }
        private Predicate<T> toStringDecorator(final Predicate<T> predicate) {
            return new Predicate<T>() {
                @Override
                public Boolean evaluate(T argument) {
                    return predicate.evaluate(argument);
                }

                @Override
                public String toString() {
                    return String.format("(%s)", predicate.toString());
                }
            };
        }
        private Iterable<Predicate<? super T>> getPredicates() {
            return new ArrayList<Predicate<? super T>>(predicates);
        }

        @Override
        public PredicateBuilder<T> orOpenBrace(Predicate<? super T> predicate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PredicateBuilder<T> andOpenBrace(Predicate<T> predicate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PredicateBuilder<T> closeBrace() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PredicateBuilder<T> openBrace(Predicate<? super T> predicate) {
            throw new UnsupportedOperationException();
        }

        public abstract void pop();
    }



    private class StackPredicateBuilderOr extends StackPredicateBuilder {
        StackPredicateBuilderOr(StackPredicateBuilder previous) {
            super(previous);
        }

        @Override
        public void pop() {
            owner().head = previous;
            owner().or(build());
        }
    }


    private class StackPredicateBuilderNone extends StackPredicateBuilder {
        StackPredicateBuilderNone() {
            super(null);
        }
        @Override
        public void pop() {
            Predicate<T> build = build();
            predicates.clear();
            predicates.add(build);
        }
    }

    private class StackPredicateBuilderAnd extends StackPredicateBuilder {
        public StackPredicateBuilderAnd(StackPredicateBuilder head) {
            super(head);
        }

        @Override
        public void pop() {
            owner().head = previous;
            owner().and(build());
        }
    }
}
