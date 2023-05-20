package com.github.liamdev06.mc.sddodgeball.game.setup;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Stores player attributes so a player can be saved and then reset.
 *
 * @param health The player's health.
 * @param healthScale The player's health scale.
 * @param foodLevel The player's hunger.
 * @param level The player's EXP bar level.
 * @param location The player's current location.
 * @param exp The player's EXP exp level.
 * @param gameMode The player's game mode.
 * @param allowFlight If the player has allowed flight turned on.
 * @param flying If the player is currently flying.
 */
public record ResettablePlayer(double health, double healthScale, int foodLevel, int level,
                               @NonNull Location location, float exp, @NonNull GameMode gameMode, boolean allowFlight,
                               boolean flying) {

    public void resetPlayer(@NonNull Player player) {
        player.setHealth(this.health);
        player.setHealthScale(this.healthScale);
        player.setFoodLevel(this.foodLevel);
        player.setLevel(this.level);
        player.teleport(this.location);
        player.setExp(this.exp);
        player.setGameMode(this.gameMode);
        player.setAllowFlight(this.allowFlight);
        player.setFlying(this.flying);
    }

}
