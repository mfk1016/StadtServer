package me.mfk1016.stadtserver.util;

public class Pair<T, U> {

    public final T _1;
    public final U _2;

    public Pair(T t, U u) {
        _1 = t;
        _2 = u;
    }

    @Override
    public String toString() {
        return "(" + _1.toString() + ", " + _2.toString() + ")";
    }
}
