package com.github.liamdev06.mc.sddodgeball.utility.messaging;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Utility and helper class to send action bar messages.
 */
public class LibActionBar {

    /**
     * Send an action bar message to a player.
     *
     * @param player An online player to send the action bar to.
     * @param message The message to send.
     */
    public static void sendActionBar(@NonNull Player player, @NonNull String message) {
        TextComponent component = new TextComponent(LibColor.colorMessage(message));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    /**
     * Clear an action bar for a player by sending an empty action bar.
     *
     * @param player An online player to remove the action bar from.
     */
    public static void clearActionBar(@NonNull Player player) {
        sendActionBar(player, "");
    }

    /**
     * Send an action bar to everyone currently online.
     *
     * @param message The message to send.
     */
    public static void sendActionBarAll(@NonNull String message) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            sendActionBar(onlinePlayer, message);
        }
    }

    /**
     * Clear the action bar for everyone currently online.
     */
    public static void clearActionBarAll() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            clearActionBar(onlinePlayer);
        }
    }
}