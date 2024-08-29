package com.covercorp.kaelaevent.util;

import java.util.HashMap;

public class NegativeSpacingCollection {
    private static final HashMap<Integer, Character> negativeSpaces = new HashMap<>() {{
        put(-1, '\uf801');
        put(-2, '\uf802');
        put(-3, '\uf803');
        put(-4, '\uf804');
        put(-5, '\uf805');
        put(-6, '\uf806');
        put(-7, '\uf807');
        put(-8, '\uf808');
        put(-16, '\uf809');
        put(-32, '\uf80a');
        put(-64, '\uf80b');
        put(-128, '\uf80c');
        put(-256, '\uf80d');
        put(-512, '\uf80e');
        put(-1024, '\uf80f');
        put(1, '\uf821');
        put(2, '\uf822');
        put(3, '\uf823');
        put(4, '\uf824');
        put(5, '\uf825');
        put(6, '\uf826');
        put(7, '\uf827');
        put(8, '\uf828');
        put(16, '\uf829');
        put(32, '\uf82a');
        put(64, '\uf82b');
        put(128, '\uf82c');
        put(256, '\uf82d');
        put(512, '\uf82e');
        put(1024, '\uf82f');
    }};

    public static String get(int number) {
        if (number == 0) return "";

        final StringBuilder negativeSpace = new StringBuilder();
        int n = Math.abs(number);

        while (n > 0) {
            int r = getMax(n);
            n -= r;
            negativeSpace.append(negativeSpaces.get(number < 0 ? r - r * 2 : r));
        }

        return negativeSpace.toString();
    }

    private static int getMax(int i) {
        int max = 0;
        for (int key : negativeSpaces.keySet()) {
            if (key > 0 && key <= i && key > max) {
                max = key;
            }
        }
        return max;
    }
}