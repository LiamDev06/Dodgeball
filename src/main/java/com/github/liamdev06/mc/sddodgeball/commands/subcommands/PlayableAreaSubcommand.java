package com.github.liamdev06.mc.sddodgeball.commands.subcommands;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.game.BlockLocationPair;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameTeam;
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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Sets the playable area for the teams.
 */
public class PlayableAreaSubcommand extends AbstractPlayerSubcommand {

    public PlayableAreaSubcommand(@NonNull DodgeballPlugin plugin) {
        super("dbadmin_playablearea");
    }

    @Override
    public void onPlayerSubcommand(@NonNull Player player, @NonNull String[] args) {
        // Checks if player has permission
        if (!PermissionHelper.hasAdminCommandPermission(player, "playablearea")) {
            MessageHelper.sendMessage(player, "command.dbadmin.no-permission", new MsgReplace("permission", "dbadmin.command.playablearea"));
            return;
        }

        // Check if the user is in creation mode
        if (!GameSetupHelper.isInCreationMode(player)) {
            player.sendMessage(LibColor.colorMessage("&c&lINVALID MODE! &cYou need to be in game setup mode to use this. Perform &6/dbadmin setupgame&c to start."));
            return;
        }

        // Check if the player has provided the correct options
        if (args.length < 3) {
            player.sendMessage(LibColor.colorMessage("&c&lMISSING INPUTS! &cYou are missing inputs, the correct command is: &6/dbadmin playablearea <teamid> <pos1/pos2>"));
            return;
        }

        // Get current setup game
        Game game = GameSetupHelper.getGameFromCreationMode(player);
        if (game == null) {
            player.sendMessage(LibColor.colorMessage("&c&lNO GAME! &cNo game could be found that you are currently setting up."));
            return;
        }

        // Get the id
        String teamId = args[1];
        if (!this.isTeamValid(game, teamId)) {
            player.sendMessage(LibColor.colorMessage("&c&lINVALID ID! &cThe team id of '&6" + teamId + "&c' does not exist!"));
            return;
        }

        // Get if position one or position two (pos1/pos2)
        String position = args[2].toLowerCase();
        if (!this.isValidPosition(position)) {
            player.sendMessage(LibColor.colorMessage("&c&lINVALID IDENTIFIER! &cPlease only use '&6pos1&c' or '&6pos2&c' as the position identifiers."));
            return;
        }

        GameTeam gameTeam = game.getTeamById(teamId);

        // Check if setting position 1 or position 2
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(LibColor.colorMessage("&c&lINVALID POSITION! &cYou need to be looking at a block to set it."));
            return;
        }

        Location positionLocation = targetBlock.getLocation();
        String friendlyLocationText = LocationHelper.simpleFriendlyLocationText(positionLocation, ChatColor.GREEN, ChatColor.GOLD);
        BlockLocationPair locationPair = gameTeam.getPlayableTeamArea();

        if (position.equals("pos1")) {
            locationPair.setPositionOne(positionLocation);
            player.sendMessage(LibColor.colorMessage("&a&lPOS1 SET! &aYou set position one to " + friendlyLocationText + "."));
        } else {
            locationPair.setPositionTwo(positionLocation);
            player.sendMessage(LibColor.colorMessage("&a&lPOS2 SET! &aYou set position one to " + friendlyLocationText + "."));
        }

        // Check if both positions are set
        if (locationPair.isBothPositionSet()) {
            player.sendMessage(LibColor.colorMessage("&2&lPOSITIONS FULL! &aBoth positions have now been set for '&6" + teamId + "&a's playable area."));
        }

        SoundHelper.playDefaultSound(player, DefaultSound.SUCCESS_LIGHT);
    }

    private boolean isTeamValid(@NonNull Game game, @NonNull String id) {
        return game.getTeamById(id) != null;
    }

    private boolean isValidPosition(@NonNull String position) {
        return position.equals("pos1") || position.equals("pos2");
    }
}
