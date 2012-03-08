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
}
