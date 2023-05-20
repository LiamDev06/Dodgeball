package com.github.liamdev06.mc.sddodgeball.commands.subcommands;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameTeam;
import com.github.liamdev06.mc.sddodgeball.game.setup.GameSetupHelper;
import com.github.liamdev06.mc.sddodgeball.utility.PermissionHelper;
import com.github.liamdev06.mc.sddodgeball.utility.SoundHelper;
import com.github.liamdev06.mc.sddodgeball.commands.core.AbstractPlayerSubcommand;
import com.github.liamdev06.mc.sddodgeball.utility.DefaultSound;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.LibColor;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MessageHelper;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MsgReplace;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Creates a new team within a game.
 */
public class CreateTeamSubCommand extends AbstractPlayerSubcommand {

    public CreateTeamSubCommand(@NonNull DodgeballPlugin plugin) {
        super("dbadmin_createteam");
    }

    @Override
    public void onPlayerSubcommand(@NonNull Player player, @NonNull String[] args) {
        // Check if the player has permission
        if (!PermissionHelper.hasAdminCommandPermission(player, "createteam")) {
            MessageHelper.sendMessage(player, "command.dbadmin.no-permission", new MsgReplace("permission", "dbadmin.command.createteam"));
            return;
        }

        // Check if the user is in creation mode
        if (!GameSetupHelper.isInCreationMode(player)) {
            player.sendMessage(LibColor.colorMessage("&c&lINVALID MODE! &cYou need to be in game setup mode to use this. Perform &6/dbadmin setupgame&c to start."));
            return;
        }

        // Check if the player has provided the correct options
        if (args.length < 5) {
            player.sendMessage(LibColor.colorMessage("&c&lMISSING INPUTS! &cYou are missing inputs, the correct command is: &6/dbadmin createteam <id> <displayName> <chatcolor> <prefix>"));
            return;
        }

        // Get current setup game
        Game game = GameSetupHelper.getGameFromCreationMode(player);
        if (game == null) {
            player.sendMessage(LibColor.colorMessage("&c&lNO GAME! &cNo game could be found that you are currently setting up."));
            return;
        }

        // Player has created the max amount of teams
        if (game.getPlayableTeams().size() >= 2) {
            player.sendMessage(LibColor.colorMessage("&c&lMAX TEAMS! &cYou have exceeded the max amount of teams, delete one before creating a new!"));
            return;
        }

        // Get the id
        String id = args[1];
        if (this.isTeamValid(game, id)) {
            player.sendMessage(LibColor.colorMessage("&c&lINVALID ID! &cThe team id of '&6" + id + "&c' already exist!"));
            return;
        }

        // Get display name
        String displayName = LibColor.colorMessage(args[2].replace("_", " "));

        // Get chat color
        String sChatColor = args[3].toUpperCase();
        if (!this.isValidChatColor(sChatColor)) {
            player.sendMessage(LibColor.colorMessage("&c&lINVALID COLOR! &cThe color with id '&6" + id + "&c' does not exist!"));
            return;
        }
        ChatColor chatColor = ChatColor.valueOf(sChatColor);

        // Get prefix
        String prefix = LibColor.colorMessage(args[4].replace("_", " "));

        // Create actual team
        GameTeam team = new GameTeam(id, displayName, chatColor, prefix, true);
        game.addTeam(team);

        // Notify player
        SoundHelper.playDefaultSound(player, DefaultSound.SUCCESS_LIGHT);
        player.sendMessage(LibColor.colorMessage("&a&lTEAM CREATED! &aYou created the team '&6" + id + "&a'."));
    }

    /**
     * @param game The game to check for.
     * @param id The team ID to check for.
     * @return Returns true if the team does not exist and false if the team exists.
     */
    private boolean isTeamValid(@NonNull Game game, @NonNull String id) {
        return game.getTeamById(id) != null;
    }

    /**
     * @param sChatColor Enum name of a chat color.
     * @return If the enum name is an entry in the {@link ChatColor} enum.
     */
    private boolean isValidChatColor(@NonNull String sChatColor) {
        try {
            ChatColor.valueOf(sChatColor);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}













