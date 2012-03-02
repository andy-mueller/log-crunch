package com.crudetech.sample.logcrunch;

class IntegerPredicates {
    static Predicate<Integer> isEven(){
        return new Predicate<Integer>() {
            @Override
            public boolean evaluate(Integer item) {
                return item % 2 == 0;
            }
        };
    }
    static Predicate<Integer> isOdd(){
        return new Predicate<Integer>() {
            @Override
            public boolean evaluate(Integer item) {
                return item % 2 != 0;
            }
        };
    }

    static Predicate<Integer> isNegative(){
        return new Predicate<Integer>() {
            @Override
            public boolean evaluate(Integer item) {
                return item < 0;
            }
        };
    }
}
