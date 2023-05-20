package com.github.liamdev06.mc.sddodgeball.utility.messaging;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Utility and helper class to send titles to players easily.
 */
public class LibTitle {

    /**
     * Send a title message with a title and subtitle to a player.
     *
     * @param player An online player to send the title message to.
     * @param fadeIn Amount of ticks to have the title fade in.
     * @param stay Amount of ticks to have the title stay on the screen for before starting to fade out.
     * @param fadeOut Amount of ticks to have the title fade out for.
     * @param title The actual title to send.
     * @param subtitle The subtitle of the title to send.
     */
    public static void sendTitle(@NonNull Player player, int fadeIn, int stay, int fadeOut, @NonNull String title, @NonNull String subtitle) {
        player.sendTitle(LibColor.colorMessage(title), LibColor.colorMessage(subtitle), fadeIn, stay, fadeOut);
    }

    /**
     * Send a title message with a title and subtitle to a player.
     *
     * @param player An online player to send the title message to.
     * @param title The actual title to send.
     * @param subtitle  The subtitle of the title to send.
     * @see LibTitle#sendTitle(Player, int, int, int, String, String)
     */
    public static void sendTitle(@NonNull Player player, @NonNull String title, @NonNull String subtitle) {
        sendTitle(player, 0, 2, 0, title, subtitle);
    }

    /**
     * Clear a title for a player by removing it from their screen.
     *
     * @param player An online player to remove the title from.
     */
    public static void clearTitle(@NonNull Player player) {
        player.resetTitle();
    }

    /**
     * Send a title message to everyone currently online.
     *
     * @param fadeIn Amount of ticks to have the title fade in.
     * @param stay Amount of ticks to have the title stay on the screen for before starting to fade out.
     * @param fadeOut Amount of ticks to have the title fade out for.
     * @param title The actual title to send.
     * @param subtitle The subtitle of the title to send.
     */
    public static void sendTitleAll(int fadeIn, int stay, int fadeOut, @NonNull String title, @NonNull String subtitle) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            sendTitle(onlinePlayer, fadeIn, stay, fadeOut, title, subtitle);
        }
    }

    /**
     * Send a title message to everyone currently online.
     *
     * @param title The actual title to send.
     * @param subtitle The subtitle of the title to send.
     * @see LibTitle#sendTitleAll(int, int, int, String, String)
     */
    public static void sendTitleAll(@NonNull String title, @NonNull String subtitle) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            sendTitle(onlinePlayer, title, subtitle);
        }
    }

    /**
     * Clear a title for everyone currently online.
     */
    public static void clearTitleAll() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            clearTitle(onlinePlayer);
        }
    }
}