package com.github.liamdev06.mc.sddodgeball.utility;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Utility and helper class when working with permissions.
 * This provides base functionality for general and wildcard permissions.
 */
public class PermissionHelper {

    private static final @NonNull String BASE_PERMISSION = "dodgeball.";
    private static final @NonNull String BASE_ADMIN_PERMISSION = "dbadmin.";
    private static final @NonNull String BASE_LOBBY_PERMISSION = "dblobby.";

    /**
     * Provides more checks than just {@link Player#hasPermission(String)} to make sure
     * certain wildcard permissions can be used regardless of permissions plugin supplier.
     *
     * @param player An online player to check for.
     * @param permission The permission to check for excluding the "dbadmin." path.
     * @return If the player has the specified permission or a wildcard addon.
     */
    public static boolean hasAdminPermission(@NonNull Player player, @NonNull String permission) {
        if (player.isOp()) {
            return true;
        }

        // Check for master wildcard permission
        if (player.hasPermission(BASE_ADMIN_PERMISSION + "*")) {
            return true;
        }

        return player.hasPermission(BASE_ADMIN_PERMISSION + permission);
    }

    /**
     * Provides more checks than just {@link Player#hasPermission(String)} to make sure
     * certain wildcard permissions can be used regardless of permissions plugin supplier.
     * This method is specific for command permissions.
     *
     * @param player An online player to check for.
     * @param command The command whose permission to check for excluding the "dbadmin.commands." path.
     * @return If the player has the specified permission or a wildcard addon.
     */
    public static boolean hasAdminCommandPermission(@NonNull Player player, @NonNull String command) {
        if (player.hasPermission(BASE_ADMIN_PERMISSION + "command.*")) {
            return true;
        }

        return hasAdminPermission(player, "command." + command);
    }

    /**
     * Provides more checks than just {@link Player#hasPermission(String)} to make sure
     * certain wildcard permissions can be used regardless of permissions plugin supplier.
     * This method checks for lobby setting bypass permissions.
     *
     * @param player An online player to check for.
     * @param setting The setting whose permission to check for excluding the "dbadmin.settings.bypass." path.
     * @return If the player has the specified permission or a wildcard addon.
     */
    public static boolean hasSettingBypassPermission(@NonNull Player player, @NonNull String setting) {
        if (player.isOp()) {
            return true;
        }

        if (player.hasPermission(BASE_LOBBY_PERMISSION + "settings.bypass.*")) {
            return true;
        }

        return player.hasPermission(BASE_LOBBY_PERMISSION + "settings.bypass." + setting);
    }

    /**
     * Provides more checks than just {@link Player#hasPermission(String)} to make sure
     * certain wildcard permissions can be used regardless of permissions plugin supplier.
     *
     * @param player An online player to check for.
     * @param permission The permission to check for excluding the "dodgeball." path.
     * @return If the player has the specified permission or a wildcard addon.
     */
    public static boolean hasPermission(@NonNull Player player, @NonNull String permission) {
        if (player.isOp()) {
            return true;
        }

        // Check for master wildcard permission
        if (player.hasPermission(BASE_PERMISSION + "*")) {
            return true;
        }

        return player.hasPermission(BASE_PERMISSION + permission);
    }

    /**
     * Provides more checks than just {@link Player#hasPermission(String)} to make sure
     * certain wildcard permissions can be used regardless of permissions plugin supplier.
     * This method is specific for command permissions.
     *
     * @param player An online player to check for.
     * @param command The command whose permission to check for excluding the "dodgeball.commands." path.
     * @return If the player has the specified permission or a wildcard addon.
     */
    public static boolean hasCommandPermission(@NonNull Player player, @NonNull String command) {
        if (player.hasPermission(BASE_PERMISSION + "command.*")) {
            return true;
        }

        return hasAdminPermission(player, "command." + command);
    }
}