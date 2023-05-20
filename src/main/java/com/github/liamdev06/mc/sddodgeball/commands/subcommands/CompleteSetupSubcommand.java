package com.github.liamdev06.mc.sddodgeball.commands.subcommands;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameState;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameTeam;
import com.github.liamdev06.mc.sddodgeball.game.setup.GameSetupHelper;
import com.github.liamdev06.mc.sddodgeball.utility.PermissionHelper;
import com.github.liamdev06.mc.sddodgeball.utility.SoundHelper;
import com.github.liamdev06.mc.sddodgeball.commands.core.AbstractPlayerSubcommand;
import com.github.liamdev06.mc.sddodgeball.utility.DefaultSound;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.LibColor;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MessageHelper;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MsgReplace;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

/**
 * Sub-command for /dbadmin. Runs checks to see if a setup is complete
 * and if it is, makes the game available to be played.
 */
public class CompleteSetupSubcommand extends AbstractPlayerSubcommand {

    public CompleteSetupSubcommand(@NonNull DodgeballPlugin plugin) {
        super("dbadmin_completesetup");
    }

    @Override
    public void onPlayerSubcommand(@NonNull Player player, @NonNull String[] args) {
        // Check if a player has permission to complete the setup
        if (!PermissionHelper.hasAdminCommandPermission(player, "completesetup")) {
            MessageHelper.sendMessage(player, "command.dbadmin.no-permission", new MsgReplace("permission", "dbadmin.command.completesetup"));
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

        // Running checks
        player.sendMessage(LibColor.colorMessage("&eRunning checks..."));
        boolean failed = false;

        // Check if waiting lobby is set
        if (this.isWaitingLobbySet(game)) {
            player.sendMessage(LibColor.colorMessage("&a● Waiting lobby set..."));
        } else {
            player.sendMessage(LibColor.colorMessage("&c● Waiting lobby not set! &c&lCONTINUE SETTING UP"));
            failed = true;
        }

        // Check if teams are created
        if (this.hasTeamsCreated(game)) {
            player.sendMessage(LibColor.colorMessage("&a● Both 2 teams are created..."));
        } else {
            player.sendMessage(LibColor.colorMessage("&c● Not enough teams are created! &c&lCONTINUE SETTING UP"));
            failed = true;
        }

        // Check if a team has a playable area
        List<GameTeam> playableTeams = game.getPlayableTeams();
        for (GameTeam team : playableTeams) {
            if (this.hasSetPlayableArea(team)) {
                player.sendMessage(LibColor.colorMessage("&a● Playable area is set for team '&6" + team.getId() + "&a'..."));
            } else {
                player.sendMessage(LibColor.colorMessage("&c● No playable area set for team '&6" + team.getId() + "&c' &c&lCONTINUE SETTING UP"));
                failed = true;
            }
        }

        if (failed) {
            player.sendMessage(LibColor.colorMessage("&c&lCHECKS FAILED! &cYou had failed checks for your game creation, continue setting up."));
            return;
        }

        // All checks done, enable the game
        game.setGameState(GameState.PRE_WAITING);
        game.setEnabled(true);
        GameSetupHelper.removePlayerFromSetupMode(player);

        // Notify player
        SoundHelper.playDefaultSound(player, DefaultSound.SUCCESS_MAJOR);
        player.sendMessage(LibColor.colorMessage("&2&lGAME CREATED! &2The game is not fully set up and ready to be played."));

        // Reset the player back to before setup mode was started
        GameSetupHelper.resetPlayer(player);
        SoundHelper.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT);
    }

    /**
     * @param game The game to check for.
     * @return If the waiting lobby has been set.
     */
    private boolean isWaitingLobbySet(@NonNull Game game) {
        return game.getWaitingLobbySpawn() != null;
    }

    /**
     * @param game The game to check for.
     * @return If two playable teams has been created.
     */
    private boolean hasTeamsCreated(@NonNull Game game) {
        return game.getPlayableTeams().size() >= 2;
    }

    /**
     * @param team The game to check for.
     * @return If both team playable areas are set.
     */
    private boolean hasSetPlayableArea(@NonNull GameTeam team) {
        return team.getPlayableTeamArea().isBothPositionSet();
    }
}













