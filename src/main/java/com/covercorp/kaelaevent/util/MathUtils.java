package com.covercorp.kaelaevent.util;

public final class MathUtils {
    public static double Distance2D(double x1, double x2, double z1, double z2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(z1 - z2, 2));
    }

    public static double Distance3D(double x1, double x2, double y1, double y2, double z1, double z2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
    }

    public static double formatDouble(double number, int precision) {
        int tmp = (int) number * (int) Math.pow(10, precision);
        return tmp / Math.pow(10, precision);
    }
}
