package com.github.liamdev06.mc.sddodgeball.utility;

import java.util.concurrent.TimeUnit;

/**
 * Utility and helper class to more nicely display timer seconds
 * to players.
 */
public class TimeDisplayHelper {

    public static String secondsToMinSecWithoutText(int input) {
        long minutes = TimeUnit.SECONDS.toMinutes(input);
        long seconds = input - minutes * 60;

        if (minutes <= 9) {
            if (seconds <= 9) {
                return "0" + minutes + ":0" + seconds;
            } else {
                return "0" + minutes + ":" + seconds;
            }
        } else {
            if (seconds <= 9) {
                return minutes + ":0" + seconds;
            }
        }

        return minutes + ":" + seconds;
    }

    public static String getTimeIcon(int second) {
        return switch (second) {
            case 1 -> "➀";
            case 2 -> "➁";
            case 3 -> "➂";
            case 4 -> "➃";
            case 5 -> "➄";
            case 6 -> "➅";
            case 7 -> "➆";
            case 8 -> "➇";
            case 9 -> "➈";
            case 10 -> "➉";
            default -> "◯";
        };
    }
}
