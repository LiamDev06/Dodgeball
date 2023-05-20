package com.github.liamdev06.mc.sddodgeball.commands.subcommands;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.commands.core.AbstractPlayerSubcommand;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.game.GameHelper;
import com.github.liamdev06.mc.sddodgeball.utility.PermissionHelper;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MessageHelper;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MsgReplace;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * Joins the first available Dodgeball game.
 */
public class JoinSubcommand extends AbstractPlayerSubcommand {

    private final @NonNull Map<String, Game> games;

    public JoinSubcommand(@NonNull DodgeballPlugin plugin) {
        super("dodgeball_join");
        this.games = plugin.getGames();
    }

    @Override
    public void onPlayerSubcommand(@NonNull Player player, @NonNull String[] args) {
        // Check if player has permission
        if (!PermissionHelper.hasPermission(player, "join")) {
            MessageHelper.sendMessage(player, "command.dodgeball.no-permission", new MsgReplace("permission", "dodgeball.command.join"));
            return;
        }

        // Cannot join a game if you are already in one
        Game game = GameHelper.getGameFromPlayer(player);
        if (game != null) {
            MessageHelper.sendMessage(player, "join-game.already-in-game");
            return;
        }

        // Loop through games and find an available one
        for (Game targetGame : this.games.values()) {
            if (targetGame.getGameState().isWaiting()) {
                MessageHelper.sendMessage(player, "join-game.game-found");
                targetGame.sendPlayer(player);
                return;
            }
        }

        // No game found
        MessageHelper.sendMessage(player, "join-game.no-games-available");
    }
}