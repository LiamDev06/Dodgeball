package com.github.liamdev06.mc.sddodgeball.game.setup;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.storage.user.IUserStorage;
import com.github.liamdev06.mc.sddodgeball.storage.user.MetadataValues;
import com.github.liamdev06.mc.sddodgeball.storage.user.User;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.LibColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility and helper class to help when setting up new games.
 */
public class GameSetupHelper {

    private static final @NonNull Map<UUID, ResettablePlayer> RESETTABLE_PLAYERS = new HashMap<>();
    private static final @NonNull IUserStorage USER_STORAGE = DodgeballPlugin.getInstance().getUserStorage();

    /**
     * Save the current information and attributes of a player. This is used
     * to be able to reset the player when they are finished creating.
     *
     * @param player An online player to save information for.
     */
    public static void savePlayer(@NonNull Player player) {
        ResettablePlayer resettablePlayer = new ResettablePlayer(
                player.getHealth(), player.getHealthScale(), player.getFoodLevel(), player.getLevel(),
                player.getLocation(), player.getExp(), player.getGameMode(),
                player.getAllowFlight(), player.isFlying()
        );

        RESETTABLE_PLAYERS.put(player.getUniqueId(), resettablePlayer);
    }

    /**
     * Resets the player back from their previous saving.
     *
     * @param player An online player to reset.
     */
    public static void resetPlayer(@NonNull Player player) {
        UUID uuid = player.getUniqueId();
        ResettablePlayer resettablePlayer = RESETTABLE_PLAYERS.get(player.getUniqueId());
        if (resettablePlayer == null) {
            return;
        }

        resettablePlayer.resetPlayer(player);
        RESETTABLE_PLAYERS.remove(uuid);
    }

    /**
     * Performs all the required checks and actions to put a player into creation mode.
     * The method gets the user and checks if they exist, then the code checks if the user is
     * already in creation mode, and if they are not, puts them into creation mode.
     *
     * @param player An online player to set into creation mode.
     */
    public static boolean setPlayerIntoSetupMode(@NonNull Player player) {
        User user = USER_STORAGE.getCachedUser(player.getUniqueId());

        // Check if the user exists within the cache
        if (user == null) {
            player.sendMessage(LibColor.colorMessage("&c&lERROR! &cYour User object could not be found."));
            return false;
        }

        // Check if the player is in creation mode
        MetadataValues metadata = user.getMetadataValues();
        if (metadata.isInGameCreationMode()) {
            player.sendMessage(LibColor.colorMessage("&c&lERROR! &cYou are already setting up a game, finish it first!"));
            return false;
        }

        // Set in creation mode
        metadata.setInCreationMode(true);
        return true;
    }

    /**
     * Performs all the required checks and actions to remove a player from creation mode.
     * The method gets the user and checks if they exist, then the code checks if the user is
     * in creation mode, and if they are, removes them from creation mode.
     *
     * @param player An online player to set into creation mode.
     */
    public static void removePlayerFromSetupMode(@NonNull Player player) {
        User user = USER_STORAGE.getCachedUser(player.getUniqueId());

        // Check if the user exists within the cache
        if (user == null) {
            player.sendMessage(LibColor.colorMessage("&c&lERROR! &cYour User object could not be found."));
            return;
        }

        // Check if the player is not creation mode
        MetadataValues metadata = user.getMetadataValues();
        if (!metadata.isInGameCreationMode()) {
            player.sendMessage(LibColor.colorMessage("&c&lERROR! &cYou are not in creation mode!"));
            return;
        }

        // Remove from creation mode
        metadata.setInCreationMode(false);
    }

    /**
     * Performs all the required checks and actions to store a game with a certain player.
     * The game stored with the player is the game they are currently creating.
     *
     * @param player An online player to link a game that they are currently creating.
     * @param game The game the player is currently creating.
     */
    public static void setGameInCreationMode(@NonNull Player player, @NonNull Game game) {
        User user = USER_STORAGE.getCachedUser(player.getUniqueId());

        // Check if the user exists within the cache
        if (user == null) {
            return;
        }

        // Set the game in creation mode
        MetadataValues metadata = user.getMetadataValues();
        metadata.setGameInCreationMode(game);
    }

    /**
     * Resets the creation for a player like removing their creation metadata values
     * and deleting the arena world.
     *
     * @param player An online player to reset their creation for.
     */
    public static void resetCreation(@NonNull Player player) {
        DodgeballPlugin plugin = DodgeballPlugin.getInstance();
        User user = USER_STORAGE.getCachedUser(player.getUniqueId());

        // Check if the user exists within the cache
        if (user == null) {
            player.sendMessage(LibColor.colorMessage("&c&lERROR! &cYour User object could not be found."));
            return;
        }

        // Check if the player is in creation mode
        MetadataValues metadata = user.getMetadataValues();
        if (!metadata.isInGameCreationMode()) {
            return;
        }

        // Reset everything
        Game game = metadata.getGameInCreationMode();
        if (game == null) {
            return;
        }
        metadata.setInCreationMode(false);

        String worldName = game.getWorldName();
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            for (Player worldPlayer : world.getPlayers()) {
                worldPlayer.kickPlayer(LibColor.colorMessage("&cThe world you were in is being unloaded and deleted."));
            }

            plugin.getWorldManager().deleteAndUnloadWorld(worldName);
        }

        plugin.removeGame(game.getGameId());
    }

    /**
     * Performs all the required checks to see if a player is in creation mode.
     *
     * @param player An online player to check creation mode for.
     * @return True if the player is in creation mode and false if the player is not.
     */
    public static boolean isInCreationMode(@NonNull Player player) {
        User user = USER_STORAGE.getCachedUser(player.getUniqueId());

        // Check if the user exists within the cache
        if (user == null) {
            player.sendMessage(LibColor.colorMessage("&c&lERROR! &cYour User object could not be found."));
            return false;
        }

        // Check if the player is in creation mode
        MetadataValues metadata = user.getMetadataValues();
        return metadata.isInGameCreationMode();
    }

    /**
     * Performs all the required checks to get the game the player is currently setting up.
     *
     * @param player An online player to get the game in creation mode.
     * @return The game the player is currently setting up.
     */
    public static @Nullable Game getGameFromCreationMode(@NonNull Player player) {
        User user = USER_STORAGE.getCachedUser(player.getUniqueId());

        // Check if the user exists within the cache
        if (user == null) {
            player.sendMessage(LibColor.colorMessage("&c&lERROR! &cYour User object could not be found."));
            return null;
        }

        // Get game from metadata
        MetadataValues metadata = user.getMetadataValues();
        return metadata.getGameInCreationMode();
    }

}
