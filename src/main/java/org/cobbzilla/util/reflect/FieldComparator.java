package org.cobbzilla.util.reflect;

import java.util.Comparator;

public class FieldComparator<T, F extends Comparable<F>> implements Comparator<T> {

    private final String field;
    private final boolean reverse = false;

    @java.beans.ConstructorProperties({"field"})
    public FieldComparator(String field) {
        this.field = field;
    }

    @Override public int compare(T o1, T o2) {
        final F v1 = (F) ReflectionUtil.get(o1, field);
        final F v2 = (F) ReflectionUtil.get(o2, field);
        return reverse
                ? (v1 == null ? (v2 == null ? 0 : 1) : (v2 == null ? 1 : v2.compareTo(v1)))
                : (v1 == null ? (v2 == null ? 0 : -1) : (v2 == null ? -1 : v1.compareTo(v2)));
    }

    public String getField() {
        return this.field;
    }

    public boolean isReverse() {
        return this.reverse;
    }
}
