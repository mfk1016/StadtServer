package me.mfk1016.stadtserver.logic.tree;

public enum Branch {
    S,
    SE,
    E,
    NE,
    N,
    NW,
    W,
    SW;

    public Branch opp() {
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
}
