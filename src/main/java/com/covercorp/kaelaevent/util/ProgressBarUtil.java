package com.covercorp.kaelaevent.util;

public class ProgressBarUtil {
    private static final String[] CHARACTERS = {
            "\uE014",  // 0
            "\uE015",  // 1
            "\uE017",  // 2
            "\uE018",  // 3
            "\uE019",  // 4
            "\uE01A",  // 5
            "\uE01B",  // 6
            "\uE01C",  // 7
            "\uE01D",  // 8
            "\uE01E",  // 9
            "\uE016"   // 10
    };

    private static final String SPECIAL_CHARACTER = "\uF801"; // 

    public static String createProgressBar(int currentProgress, int targetProgress, int length) {
        if (currentProgress < 0) {
            currentProgress = 0;
        } else if (currentProgress > targetProgress) {
            currentProgress = targetProgress;
        }

        // Calculate the percentage based on current and target progress
        double percentage = (currentProgress / (double) targetProgress) * 100;

        final StringBuilder bar = new StringBuilder();

        // Calculate the number of full segments and the level of the last segment
        double percentagePerSegment = 100.0 / length;
        int fullSegments = (int) (percentage / percentagePerSegment);
        int lastSegmentLevel = (int) ((percentage % percentagePerSegment) / (percentagePerSegment / 10.0));

        for (int i = 0; i < length; i++) {
            if (i < fullSegments) {
                bar.append("<font:kaela:boss_bar>").append(CHARACTERS[10]).append("</font>");
            } else if (i == fullSegments) {
                bar.append("<font:kaela:boss_bar>").append(CHARACTERS[lastSegmentLevel]).append("</font>");
            } else {
                bar.append("<font:kaela:boss_bar>").append(CHARACTERS[0]).append("</font>");
            }
            // Add the special character after each segment except the last
            if (i < length - 1) {
                bar.append("<font:minecraft:default>").append(SPECIAL_CHARACTER).append("</font>");
            }
        }
        return bar.toString();
    }

    public static String createVanillaProgressBar(int currentProgress, int targetProgress, int length) {
        if (currentProgress < 0) {
            currentProgress = 0;
        } else if (currentProgress > targetProgress) {
            currentProgress = targetProgress;
        }

        double percentage = (currentProgress / (double) targetProgress) * 100;

        final StringBuilder bar = new StringBuilder();

        double percentagePerSegment = 100.0 / length;
        int fullSegments = (int) (percentage / percentagePerSegment);

        for (int i = 0; i < length; i++) {
            if (i < fullSegments) {
                bar.append("<green>■</green>");
            } else {
                bar.append("<red>□</red>");
            }
        }
        return bar.toString();
    }
}
