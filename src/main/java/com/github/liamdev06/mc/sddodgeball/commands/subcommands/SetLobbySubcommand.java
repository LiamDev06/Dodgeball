package com.github.liamdev06.mc.sddodgeball.commands.subcommands;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.managers.ScoreboardManager;
import com.github.liamdev06.mc.sddodgeball.storage.GameFileStorage;
import com.github.liamdev06.mc.sddodgeball.utility.location.LocationHelper;
import com.github.liamdev06.mc.sddodgeball.utility.PermissionHelper;
import com.github.liamdev06.mc.sddodgeball.utility.SoundHelper;
import com.github.liamdev06.mc.sddodgeball.commands.core.AbstractPlayerSubcommand;
import com.github.liamdev06.mc.sddodgeball.utility.DefaultSound;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.LibColor;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MessageHelper;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MsgReplace;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Sets the main server game lobby.
 */
public class SetLobbySubcommand extends AbstractPlayerSubcommand {

    private final @NonNull DodgeballPlugin plugin;
    private final @NonNull GameFileStorage lobbyConfig;

    public SetLobbySubcommand(@NonNull DodgeballPlugin plugin) {
        super("dbadmin_setlobby");
        this.plugin = plugin;
        this.lobbyConfig = plugin.getLobbyConfig();
    }

    @Override
    public void onPlayerSubcommand(@NonNull Player player, @NonNull String[] args) {
        // Checks if player has permission
        if (!PermissionHelper.hasAdminCommandPermission(player, "setlobby")) {
            MessageHelper.sendMessage(player, "command.dbadmin.no-permission", new MsgReplace("permission", "dbadmin.command.setlobby"));
            return;
        }

        // Get the location and serialize it to a string
        final Location location = player.getLocation();
        String serializedLocation = LocationHelper.writeLocation(location);

        // Set the server lobby to the player's current location
        this.lobbyConfig.setAsync("environment.lobby", serializedLocation).thenAccept((v) -> {
            ScoreboardManager manager = this.plugin.getScoreboardManager();

            // Apply to players in the world
            World world = location.getWorld();
            if (world != null) {
                for (Player target : world.getPlayers()) {
                    manager.applyLobbyScoreboard(target);
                }
            }

            SoundHelper.playDefaultSound(player, DefaultSound.SUCCESS_LIGHT);
            String friendlyLobbyLocation = LocationHelper.friendlyLocationText(location, ChatColor.GREEN, ChatColor.GOLD);
            player.sendMessage(LibColor.colorMessage("&a&lLOBBY SET! &aThe lobby location was set to " + friendlyLobbyLocation + "."));
        });
    }
}











