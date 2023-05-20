package com.github.liamdev06.mc.sddodgeball.commands.subcommands;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.game.setup.GameSetupHelper;
import com.github.liamdev06.mc.sddodgeball.utility.PermissionHelper;
import com.github.liamdev06.mc.sddodgeball.utility.SoundHelper;
import com.github.liamdev06.mc.sddodgeball.commands.core.AbstractPlayerSubcommand;
import com.github.liamdev06.mc.sddodgeball.utility.DefaultSound;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.LibColor;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MessageHelper;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MsgReplace;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Deletes a team while setting up a game.
 */
public class DeleteTeamSubcommand extends AbstractPlayerSubcommand {

    public DeleteTeamSubcommand(@NonNull DodgeballPlugin plugin) {
        super("dbadmin_deleteteam");
    }

    @Override
    public void onPlayerSubcommand(@NonNull Player player, @NonNull String[] args) {
        // Checks if the player has permission
        if (!PermissionHelper.hasAdminCommandPermission(player, "deleteteam")) {
            MessageHelper.sendMessage(player, "command.dbadmin.no-permission", new MsgReplace("permission", "dbadmin.command.deleteteam"));
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

        // No teams exist
        if (game.getPlayableTeams().size() == 0) {
            player.sendMessage(LibColor.colorMessage("&c&lNO TEAMS! &cThere are no teams created!"));
            return;
        }

        // Check if the player has provided the correct options
        if (args.length < 2) {
            player.sendMessage(LibColor.colorMessage("&c&lMISSING INPUTS! &cYou are missing inputs, the correct command is: &6/dbadmin deleteteam <id>"));
            return;
        }

        // Get the id
        String id = args[1];
        if (!this.isTeamValid(game, id)) {
            player.sendMessage(LibColor.colorMessage("&c&lINVALID ID! &cThere is no team with the id of '&6" + id + "&c'!"));
            return;
        }

        // Delete the team
        game.removeTeam(id);

        // Notify player
        SoundHelper.playDefaultSound(player, DefaultSound.SUCCESS_LIGHT);
        player.sendMessage(LibColor.colorMessage("&e&lTEAM DELETED! &eYou deleted the team '&6" + id + "&e'."));
    }

    /**
     * @param game The game to check for.
     * @param id The team ID to check for.
     * @return Returns true if the team does not exist and false if the team exists.
     */
    private boolean isTeamValid(@NonNull Game game, @NonNull String id) {
        return game.getTeamById(id) != null;
    }
}













