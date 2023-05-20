package com.github.liamdev06.mc.sddodgeball.game;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class GameHelper {

    private static final @NonNull DodgeballPlugin PLUGIN = DodgeballPlugin.getInstance();

    /**
     * Get a Game object instance from a player who is playing in a game
     *
     * @param player The player to get the game from.
     * @return The game the target player is playing in.
     */
    public static @Nullable Game getGameFromPlayer(@NonNull Player player) {
        UUID uuid = player.getUniqueId();

        for (Game game : PLUGIN.getGames().values()) {
            if (game.inGame(uuid)) {
                return game;
            }
        }

        return null;
    }
}
