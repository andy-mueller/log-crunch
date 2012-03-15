package com.crudetech.sample;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Iterables {
    public static <T> List<T> copy(Iterator<T> src) {
        ArrayList<T> rv = new ArrayList<T>();
        while (src.hasNext()) {
            rv.add(src.next());
        }
        return rv;
    }
    public static <T> List<T> copy(Iterable<T> src) {
        return copy(src.iterator());
    }

    public static int size(Iterable<?> iterable) {
        int count = 0;
        Iterator<?> it = iterable.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (it.hasNext()) {
            count++;
            it.next();
        }
        return count;
    }

    public static <T> T getFirst(Iterable<T> i) {
        return i.iterator().next();
    }
}
