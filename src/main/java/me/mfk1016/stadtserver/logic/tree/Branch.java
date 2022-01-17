package me.mfk1016.stadtserver.logic.tree;

import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.block.BlockFace;
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

    public Branch twoForward() {
        return switch (this) {
            case S -> E;
            case E -> N;
            case N -> W;
            case W -> S;
            case SE -> NE;
            case NE -> NW;
            case NW -> SW;
            case SW -> SE;
        };
    }

    public BlockFace toFace() {
        return switch (this) {
            case S -> BlockFace.SOUTH;
            case E -> BlockFace.EAST;
            case N -> BlockFace.NORTH;
            case W -> BlockFace.WEST;
            case SE -> BlockFace.SOUTH_EAST;
            case NE -> BlockFace.NORTH_EAST;
            case NW -> BlockFace.NORTH_WEST;
            case SW -> BlockFace.SOUTH_WEST;
        };
    }

    public Pair<Vector, Vector> getSphereQuadrant(double yAngleDown, double yAngleUp, double spread) {
        assert 0 <= spread && spread <= 1;
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
        spread = spread * 20D;
        angleA = Math.toRadians(dir - spread);
        angleB = Math.toRadians(dir + spread);
        double yd = Math.toRadians(90D - yAngleDown);
        double yu = Math.toRadians(90D - yAngleUp);

        // cos = X, sin = Z
        Vector a = new Vector(Math.cos(angleA) * Math.sin(yd), Math.cos(yd), Math.sin(angleA) * Math.sin(yd));
        Vector b = new Vector(Math.cos(angleB) * Math.sin(yu), Math.cos(yu), Math.sin(angleB) * Math.sin(yu));
        return new Pair<>(a, b);
    }

    public static Branch[] cardinals() {
        return new Branch[]{S, E, N, W};
    }
}
