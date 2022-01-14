package me.mfk1016.stadtserver.logic.tree;

import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.util.Vector;

public enum Branch {
    S,
    SE,
    E,
    NE,
    N,
    NW,
    W,
    SW;

    public Branch invert() {
        return switch (this) {
            case S -> N;
            case E -> W;
            case N -> S;
            case W -> E;
            case SE -> NW;
            case NE -> SW;
            case NW -> SE;
            case SW -> NE;
        };
    }

    public Pair<Branch, Branch> neighbors() {
        return switch (this) {
            case S -> new Pair<>(SW, SE);
            case E -> new Pair<>(SE, NE);
            case N -> new Pair<>(NE, NW);
            case W -> new Pair<>(NW, SW);
            case SE -> new Pair<>(S, E);
            case NE -> new Pair<>(E, N);
            case NW -> new Pair<>(N, W);
            case SW -> new Pair<>(W, S);
        };
    }

    public Pair<Vector, Vector> getSphereQuadrant(double yAngleDown, double yAngleUp) {
        double angleA, angleB;
        double dir = switch (this) {
            case E -> 0D;
            case NE -> 45D;
            case N -> 90D;
            case NW -> 135D;
            case W -> 180D;
            case SW -> 225D;
            case S -> 270D;
            case SE -> 315D;
        };
        angleA = Math.toRadians(dir - 20D);
        angleB = Math.toRadians(dir + 20D);
        double yd = Math.toRadians(90D - yAngleDown);
        double yu = Math.toRadians(90D - yAngleUp);

        // cos = X, sin = Z
        Vector a = new Vector(Math.cos(angleA) * Math.sin(yd), Math.cos(yd), Math.sin(angleA) * Math.sin(yd));
        Vector b = new Vector(Math.cos(angleB) * Math.sin(yu), Math.cos(yu), Math.sin(angleB) * Math.sin(yu));
        return new Pair<>(a, b);
    }
}
