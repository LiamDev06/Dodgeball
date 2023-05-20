package com.github.liamdev06.mc.sddodgeball.utility.messaging;

import net.md_5.bungee.api.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility and helper class to colorize messages with both regular bukkit color codes
 * as well as hex coloring.
 */
public class LibColor {

    private static final @NonNull String RAW_HEX_REGEX = "\\{#[A-Fa-f0-9]{6}}";

    /**
     * Colorize a message with regular bukkit color codes using the '&' symbol,
     * and by colorizing it with hex colors.
     *
     * @param input The message to colorize.
     * @return The colorized message.
     */
    public static String colorMessage(@NonNull String input) {
        // Regular bukkit coloring
        input = org.bukkit.ChatColor.translateAlternateColorCodes('&', input);

        // Colorize with hex color
        input = hex(input);

        return input;
    }

    /**
     * Colorizes a message with hex colors. To use hex colors,
     * follow the format of {#HEX_CODE}, in the message string.
     *
     * @param input The message to colorize.
     * @return The colorized message.
     */
    private static String hex(@NonNull String input) {
        Matcher matcher = Pattern.compile(RAW_HEX_REGEX).matcher(input);

        // Get amount of hexes in the message
        int hexAmount = 0;
        while (matcher.find()) {
            matcher.region(matcher.end() - 1, input.length());
            hexAmount++;
        }

        // Go through message and look for hex color
        int startIndex = 0;
        for (int hexIndex = 0; hexIndex < hexAmount; hexIndex++) {
            int msgIndex = input.indexOf("{#", startIndex);
            String hex = input.substring(msgIndex + 1, msgIndex + 8);
            startIndex = msgIndex + 2;

            // Replace with hex color
            input = input.replace("{" + hex + "}", ChatColor.of(hex) + ""); // Replaces the hex code with the color
        }

        return input;
    }

    /**
     * De-color and remove the color from an input string.
     *
     * @param input The message to remove the color from.
     * @return The de-colored version of the message.
     */
    public static String decolor(@NonNull String input) {
        return ChatColor.stripColor(input);
    }
}
