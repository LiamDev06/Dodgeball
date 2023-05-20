package com.github.liamdev06.mc.sddodgeball.listeners;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.storage.user.storage.IUserStorage;
import com.github.liamdev06.mc.sddodgeball.storage.GameFileStorage;
import com.github.liamdev06.mc.sddodgeball.game.setup.GameSetupHelper;
import com.github.liamdev06.mc.sddodgeball.utility.PermissionHelper;
import com.github.liamdev06.mc.sddodgeball.managers.ScoreboardManager;
import com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegister;
import com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * All specific listeners for the server lobby.
 */
@AutoRegister(type = AutoRegistry.Type.LISTENER)
public class LobbyListener implements Listener {

    private static final @NonNull String SETTINGS_PATH = "settings.";

    private final @NonNull DodgeballPlugin plugin;
    private final @NonNull IUserStorage userManager;
    private final @NonNull GameFileStorage lobbyConfig;
    private final @NonNull ScoreboardManager scoreboardManager;

    public LobbyListener(@NonNull DodgeballPlugin plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserStorage();
        this.lobbyConfig = plugin.getLobbyConfig();
        this.scoreboardManager = plugin.getScoreboardManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        // Create a user if it does not exist
        this.userManager.getUser(uuid).thenAccept(user -> {
            if (user == null) {
                this.userManager.createNewUser(uuid).thenAccept(newUser -> newUser.setLevel(1));
            }
        });

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            // If lobby settings are enabled, apply effects
            if (this.isLobbySettingsEnabled()) {
                this.applyJoinEffects(player);
            }

            // If scoreboard is enabled, create a new scoreboard for the player and set it
            if (this.scoreboardManager.isLobbyScoreboardEnabled() && this.isInLobbyWorld(player)) {
                this.scoreboardManager.applyLobbyScoreboard(player);
            }
        }, 4);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        // Remove scoreboard
        this.scoreboardManager.clearLobbyScoreboard(player);

        // If the user is in creation mode, reset everything they created
        if (GameSetupHelper.isInCreationMode(player)) {
            GameSetupHelper.resetCreation(player);
        }

        // Handle user
        this.userManager.getUser(uuid).thenAccept(user -> {
            if (user != null) {
                this.userManager.saveUserToStorage(uuid);
                this.userManager.removeUserFromCache(uuid);
            }
        });
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();

        // If lobby settings are enabled, teleport the player to the spawn and apply effects
        if (this.isLobbySettingsEnabled() && this.isInLobbyWorld(player)) {
            this.applyJoinEffects(player);
        }

        // If the user is in creation mode, reset everything they created
        if (GameSetupHelper.isInCreationMode(player)) {
            Game game = GameSetupHelper.getGameFromCreationMode(player);
            if (game == null) {
                return;
            }

            if (!player.getWorld().getName().equals(game.getWorldName())) {
                GameSetupHelper.resetCreation(player);
            }
        }

        // Check if scoreboard is enabled
        if (this.scoreboardManager.isLobbyScoreboardEnabled()) {
            // Player joined the lobby, add the scoreboard
            if (this.isInLobbyWorld(player)) {
                this.scoreboardManager.applyLobbyScoreboard(player);
            } else {
                // Player left the lobby, remove the scoreboard
                this.scoreboardManager.clearLobbyScoreboard(player);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        // Check if the entity is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Check if lobby settings are enabled, if the player is in the lobby world and if the player has bypass permission
        if (!this.isLobbySettingsEnabled() || !this.isInLobbyWorld(player) || PermissionHelper.hasSettingBypassPermission(player, "allow-hunger")) {
            return;
        }

        String allowHungerPath = SETTINGS_PATH + "allow-hunger";
        if (this.lobbyConfig.ensureBoolean(allowHungerPath)) {
            if (!this.lobbyConfig.getBoolean(allowHungerPath)) {
                // Cancel food level change event
                event.setCancelled(true);
            }
        } else {
            this.plugin.getLogger().warning("The allow hunger value in lobby.yml at " + allowHungerPath + " is invalid!");
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        // Check if the entity is a player
        final Player player = event.getPlayer();

        // Check if lobby settings are enabled, if the player is in the lobby world and if the player has bypass permission
        if (!this.isLobbySettingsEnabled() || !this.isInLobbyWorld(player) || PermissionHelper.hasSettingBypassPermission(player, "allow-item-drop")) {
            return;
        }

        String allowHungerPath = SETTINGS_PATH + "allow-item-drop";
        if (this.lobbyConfig.ensureBoolean(allowHungerPath)) {
            if (!this.lobbyConfig.getBoolean(allowHungerPath)) {
                // Cancel the item drop event
                event.setCancelled(true);
            }
        } else {
            this.plugin.getLogger().warning("The allow item drop value in lobby.yml at " + allowHungerPath + " is invalid!");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Check if lobby settings are enabled, if the player is in the lobby world and if the player has bypass permission
        if (!this.isLobbySettingsEnabled() || !this.isInLobbyWorld(player)) {
            return;
        }

        String allowHungerPath = SETTINGS_PATH + "allow-player-damage";
        if (this.lobbyConfig.ensureBoolean(allowHungerPath)) {
            if (!this.lobbyConfig.getBoolean(allowHungerPath)) {
                // Cancel the damage event
                event.setCancelled(true);
            }
        } else {
            this.plugin.getLogger().warning("The allow player damage value in lobby.yml at " + allowHungerPath + " is invalid!");
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (this.isLobbySettingsEnabled() && this.isLobbyWorld(event.getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();

        if (this.isLobbySettingsEnabled() && this.isInLobbyWorld(player) && !PermissionHelper.hasSettingBypassPermission(player, "allow-block-breaking")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();

        if (this.isLobbySettingsEnabled() && this.isInLobbyWorld(player) && !PermissionHelper.hasSettingBypassPermission(player, "allow-block-placing")) {
            event.setCancelled(true);
        }
    }

    /**
     * Applies all the join effects found in lobby.yml.
     *
     * @param player An online player to provide the effects to.
     */
    private void applyJoinEffects(@NonNull Player player) {
        Logger logger = this.plugin.getLogger();

        // Teleport the player to the spawn
        String lobbyPath = "environment.lobby";
        Location location = this.lobbyConfig.getLocation(lobbyPath, false);
        if (location == null) {
            logger.warning("The location in lobby.yml at " + lobbyPath + " is not set or invalid!");
        } else {
            player.teleport(location);
        }

        // Apply game mode
        String gameModePath = SETTINGS_PATH + "on-join.set-gamemode";
        try {
            GameMode gameMode = GameMode.valueOf(this.lobbyConfig.getString(gameModePath).toUpperCase());
            player.setGameMode(gameMode);
        } catch (IllegalArgumentException exception) {
            logger.severe("The Game Mode set in lobby.yml at " + gameModePath + " is invalid!");
        }

        // Set health
        String healthPath = SETTINGS_PATH + "on-join.set-health";
        if (this.lobbyConfig.ensureInt(healthPath)) {
            int health = this.lobbyConfig.getInt(healthPath);

            // Set health scale and health for the player
            player.setHealthScale(health);
            player.setHealth(health);
        } else {
            logger.warning("The health value in lobby.yml at " + healthPath + " is not a number!");
        }

        // Set food level
        String foodLevelPath = SETTINGS_PATH + "on-join.set-food";
        if (this.lobbyConfig.ensureInt(foodLevelPath)) {
            // Set food level for the player
            player.setFoodLevel(this.lobbyConfig.getInt(foodLevelPath));
        } else {
            logger.warning("The food level value in lobby.yml at " + foodLevelPath + " is not a number!");
        }

        // Clear inventory
        String clearInventoryPath = SETTINGS_PATH + "on-join.clear-inventory";
        if (this.lobbyConfig.ensureBoolean(clearInventoryPath)) {
            if (this.lobbyConfig.getBoolean(clearInventoryPath)) {
                // Clear inventory
                player.getInventory().clear();
                player.updateInventory();
            }
        } else {
            logger.warning("The clear inventory value in lobby.yml at " + clearInventoryPath + " is invalid!");
        }
    }

    /**
     * @param player The world from the player.
     * @see LobbyListener#isLobbyWorld(World)
     * @return If the world the player is in is the same world as the world in the lobby configuration spawn.
     */
    private boolean isInLobbyWorld(@NonNull Player player) {
        return this.isLobbyWorld(player.getWorld());
    }

    /**
     * @param world The world to check for.
     * @return If the {@param world} is the same world as the world in the lobby configuration spawn.
     */
    private boolean isLobbyWorld(@NonNull World world) {
        Location lobbyLocation = this.lobbyConfig.getLocation("environment.lobby", false);
        if (lobbyLocation == null) {
            return false;
        }

        World targetWorld = lobbyLocation.getWorld();
        return world.equals(targetWorld);
    }

    /**
     * @return If lobby settings are enabled by the lobby configuration.
     */
    private boolean isLobbySettingsEnabled() {
        // Check if the "enable settings" is a boolean
        if (!this.lobbyConfig.ensureBoolean(SETTINGS_PATH + "enable-settings")) {
            return false;
        }

        return this.lobbyConfig.getBoolean(SETTINGS_PATH + "enable-settings");
    }
}