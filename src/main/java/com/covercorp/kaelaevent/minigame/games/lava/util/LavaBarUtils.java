package com.covercorp.kaelaevent.minigame.games.lava.util;

public final class LavaBarUtils {
    private final static String BASE_BAR_CHARACTER = "▊";
    private final static String RED = "<red>▊";
    private final static String YELLOW = "<yellow>";
    private final static String GREEN = "<green>";
    private final static String GRAY = "<gray>";

    public static String getBar(int timeLeft, int baseTime, int segments) {
        if (timeLeft <= 0) {
            return RED.repeat(segments);
        }

        int totalLength = segments;
        int timeRemainingPercentage = Math.max(0, (int) ((timeLeft / (double) baseTime) * 100));

        StringBuilder bar = new StringBuilder();

        for (int i = 0; i < totalLength; i++) {
            int segmentPercentage = (i + 1) * 100 / totalLength;

            if (segmentPercentage <= timeRemainingPercentage) {
                bar.append(GREEN);
            } else {
                bar.append(GRAY);
            }
            bar.append(BASE_BAR_CHARACTER);
        }

        return bar.toString();
    }
}
