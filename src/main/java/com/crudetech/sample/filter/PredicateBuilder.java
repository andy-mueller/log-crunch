package com.crudetech.sample.filter;

import java.util.ArrayList;
import java.util.List;

public abstract class PredicateBuilder<T> {

    public static <T> PredicateBuilder<T> forClass(Class<T> clazz) {
        return new StackPredicateBuilder<T>();
    }

    public abstract PredicateBuilder<T> start(Predicate<? super T> predicate);

    public abstract PredicateBuilder<T> or(Predicate<? super T> predicate);

    public abstract PredicateBuilder<T> and(Predicate<? super T> predicate);

    public abstract Predicate<T> build();

    public abstract PredicateBuilder<T> orOpenBrace(Predicate<? super T> predicate);

    public abstract PredicateBuilder<T> closeBrace();

    private static class StackPredicateBuilder<T> extends PredicateBuilder<T> {
        private StackElementPredicateBuilder<T> head;

        @Override
        public PredicateBuilder<T> start(Predicate<? super T> predicate) {
            head = new StackElementPredicateBuilder<T>(null);
            head.start(predicate);
            return this;
        }

        @Override
        public PredicateBuilder<T> or(Predicate<? super T> predicate) {
            head.or(predicate);
            return this;
        }

        @Override
        public PredicateBuilder<T> and(Predicate<? super T> predicate) {
            head.and(predicate);
            return this;
        }

        @Override
        public Predicate<T> build() {
            return head.build();
        }

        @Override
        public PredicateBuilder<T> orOpenBrace(Predicate<? super T> predicate) {
            head = new StackElementPredicateBuilder<T>(ConcatOp.Or, head);
            return this;
        }

        @Override
        public PredicateBuilder<T> closeBrace() {
            head.closeBrace();
            head = head.previous;
            return this;
        }
    }

    enum ConcatOp {
        None {
            @Override
            public <T> void concat(StackElementPredicateBuilder<T> element, Predicate<T> predicates) {
                throw new UnsupportedOperationException();
            }
        }, Or {
            @Override
            public <T> void concat(StackElementPredicateBuilder<T> element, Predicate<T> predicates) {
                element.or(predicates);
            }
        }, And {
            @Override
            public <T> void concat(StackElementPredicateBuilder<T> element, Predicate<T> predicates) {
                element.and(predicates);
            }
        };

        public abstract <T> void concat(StackElementPredicateBuilder<T> element, Predicate<T> predicates);
    }
    private static class StackElementPredicateBuilder<T> extends PredicateBuilder<T> {
        private final List<Predicate<T>> predicates = new ArrayList<Predicate<T>>();
        private final ConcatOp concatOp;
        private final StackElementPredicateBuilder<T> previous;


        public StackElementPredicateBuilder(StackElementPredicateBuilder<T> previous) {
            this(ConcatOp.None, previous);
        }

        public StackElementPredicateBuilder(ConcatOp concatOp, StackElementPredicateBuilder<T> previous) {
            this.concatOp = concatOp;
            this.previous = previous;
        }

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
            Predicate<T> and = Predicates.and(getLastPredicate(), super_cast(predicate));
            this.predicates.set(this.predicates.size() - 1, and);
            return this;
        }

        private Predicate<? super T> getLastPredicate() {
            return this.predicates.get(this.predicates.size() - 1);
        }

        public Predicate<T> build() {
            return Predicates.or(getOrPredicates());
        }


        @Override
        public PredicateBuilder<T> orOpenBrace(Predicate<? super T> tPredicate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PredicateBuilder<T> closeBrace() {
            Predicate<T> predicates = build();
            concatOp.concat(previous, predicates);
            return this;
        }

        //Stack ----(head)---->elem-----(prev)----->elem-----(prev)----->elem
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
}
