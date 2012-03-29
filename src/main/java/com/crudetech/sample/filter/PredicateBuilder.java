package com.crudetech.sample.filter;

import java.util.ArrayList;
import java.util.List;

public class PredicateBuilder<T> {
    private StackPredicateBuilder<T> head;
    private int braceCount = 0;

    public PredicateBuilder<T> start(Predicate<? super T> predicate) {
        head = new StackPredicateBuilderNone<T>(this);
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
        if (braceCount != 0) {
            throw new IllegalStateException("There are still open braces!");
        }
        return getHead().build();
    }

    private StackPredicateBuilder<T> getHead() {
        if (head == null) {
            throw new IllegalStateException("Head was not set. this builder was not started correctly!");
        }
        return head;
    }

    public PredicateBuilder<T> orOpenBrace(Predicate<? super T> predicate) {
        if (head == null) {
            return openBrace(predicate);
        }
        return openBrace(new StackPredicateBuilderOr<T>(this, head), predicate);
    }

    public PredicateBuilder<T> andOpenBrace(Predicate<T> predicate) {
        if (head == null) {
            return openBrace(predicate);
        }
        return openBrace(new StackPredicateBuilderAnd<T>(this, head), predicate);
    }

    public PredicateBuilder<T> closeBrace() {
        verifyBracesWereOpened();
        --braceCount;
        head.pop();
        return this;
    }

    private void verifyBracesWereOpened() {
        if (braceCount <= 0) {
            throw new IllegalStateException("There were no braces opened");
        }
    }

    public PredicateBuilder<T> openBrace(Predicate<? super T> predicate) {
        verifyNotStarted();
        openBrace(new StackPredicateBuilderNone<T>(this), predicate);
        return this;
    }

    private void verifyNotStarted() {
        if (head != null) {
            throw new IllegalStateException("This builder was already started!");
        }
    }

    private PredicateBuilder<T> openBrace(StackPredicateBuilder<T> newHead, Predicate<? super T> predicate) {
        head = newHead;
        ++braceCount;
        if (predicate != null) {
            head.start(predicate);
        }
        return this;
    }

    public PredicateBuilder<T> openBrace() {
        verifyNotStarted();
        openBrace(new StackPredicateBuilderNone<T>(this), null);
        return this;
    }

    public PredicateBuilder<T> andOpenBrace() {
        return andOpenBrace(null);
    }

    public PredicateBuilder<T> orOpenBrace() {
        return orOpenBrace(null);
    }


    private abstract static class StackPredicateBuilder<T> extends PredicateBuilder<T> {
        final List<Predicate<T>> predicates = new ArrayList<Predicate<T>>();
        final StackPredicateBuilder<T> previous;
        private final PredicateBuilder<T> outerThis;

        StackPredicateBuilder(PredicateBuilder<T> outerThis, StackPredicateBuilder<T> previous) {
            this.previous = previous;
            this.outerThis = outerThis;
        }

        @Override
        public PredicateBuilder<T> start(Predicate<? super T> predicate) {
            predicates.add(super_away(predicate));
            return owner();
        }

        PredicateBuilder<T> owner() {
            return outerThis;
        }

        private Predicate<T> super_away(final Predicate<? super T> predicate) {
            return new Predicate<T>() {
                @Override
                public Boolean evaluate(T argument) {
                    return predicate.evaluate(argument);
                }

                @Override
                public String toString() {
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
            if (!predicates.isEmpty()) {
                Predicate<T> and = Predicates.and(lastPredicate(), predicate);
                replaceLastPredicate(and);
            } else {
                predicates.add(super_away(predicate));
            }
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


    private static class StackPredicateBuilderOr<T> extends StackPredicateBuilder<T> {

        StackPredicateBuilderOr(PredicateBuilder<T> outerThis, StackPredicateBuilder<T> previous) {
            super(outerThis, previous);
        }

        @Override
        public void pop() {
            owner().head = previous;
            owner().or(build());
        }
    }


    private static class StackPredicateBuilderNone<T> extends StackPredicateBuilder<T> {
        StackPredicateBuilderNone(PredicateBuilder<T> outerThis) {
            super(outerThis, null);
        }

        @Override
        public void pop() {
            Predicate<T> build = build();
            predicates.clear();
            predicates.add(build);
        }
    }

    private static class StackPredicateBuilderAnd<T> extends StackPredicateBuilder<T> {
        public StackPredicateBuilderAnd(PredicateBuilder<T> outerThis, StackPredicateBuilder<T> head) {
            super(outerThis, head);
        }

        @Override
        public void pop() {
            owner().head = previous;
            owner().and(build());
        }
    }
}
