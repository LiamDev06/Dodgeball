package com.github.liamdev06.mc.sddodgeball.commands.subcommands;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.game.setup.GameSetupHelper;
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
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Sets the waiting lobby for a game in setup mode.
 */
public class SetWaitingLobbySubcommand extends AbstractPlayerSubcommand {

    public SetWaitingLobbySubcommand(@NonNull DodgeballPlugin plugin) {
        super("dbadmin_setwaitinglobby");
    }

    @Override
    public void onPlayerSubcommand(@NonNull Player player, @NonNull String[] args) {
        if (!PermissionHelper.hasAdminCommandPermission(player, "setwaitinglobby")) {
            MessageHelper.sendMessage(player, "command.dbadmin.no-permission", new MsgReplace("permission", "dbadmin.command.setwaitinglobby"));
            return;
        }

        // Check if the user is in creation mode
        if (!GameSetupHelper.isInCreationMode(player)) {
            player.sendMessage(LibColor.colorMessage("&c&lINVALID MODE! &cYou need to be in game setup mode to use this. Perform &6/dbadmin setupgame&c to start."));
            return;
        }

        // Get current setup game
        Game game = GameSetupHelper.getGameFromCreationMode(player);
        if (game == null) {
            player.sendMessage(LibColor.colorMessage("&c&lNO GAME! &cNo game could be found that you are currently setting up."));
            return;
        }

        // Set the waiting lobby spawn
        Location location = player.getLocation();
        game.setWaitingLobbySpawn(location);

        // Notify player
        String friendlyLocation = LocationHelper.simpleFriendlyLocationText(location, ChatColor.GREEN, ChatColor.GOLD);
        SoundHelper.playDefaultSound(player, DefaultSound.SUCCESS_LIGHT);
        player.sendMessage(LibColor.colorMessage("&a&lGAME LOBBY SET! &aThe waiting lobby location was set to " + friendlyLocation + "."));
    }
}











