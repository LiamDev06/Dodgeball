package com.github.liamdev06.mc.sddodgeball;

import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.game.GameHelper;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameTeam;
import com.github.liamdev06.mc.sddodgeball.storage.user.User;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class PlaceholderAPIExtension extends PlaceholderExpansion {

    private static final @NonNull String INVALID = "[invalid]";

    private final @NonNull DodgeballPlugin plugin;

    public PlaceholderAPIExtension(@NonNull DodgeballPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NonNull
    public String getAuthor() {
        return "LiamH";
    }

    @Override
    @NonNull
    public String getIdentifier() {
        return "dodgeball";
    }

    @Override
    @NonNull
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NonNull String params) {
        User user = this.plugin.getUserStorage().getCachedUser(player.getUniqueId());
        if (user == null) {
            return "";
        }

        // Members on each side if the player is in a game
        // Returns a string like follows: TeamOneDisplayName: TeamOnePlayersLeft - TeamTwoDisplayName: TeamTwoPlayersLeft
        if (params.equals("playersleft")) {
            Player target = Bukkit.getPlayer(player.getUniqueId());
            if (target == null) {
                return INVALID;
            }

            // Get the game from the online player
            Game game = GameHelper.getGameFromPlayer(target);
            if (game == null) {
                return INVALID;
            }

            List<GameTeam> playableTeams = game.getPlayableTeams();
            if (playableTeams.size() != 2) {
                return INVALID;
            }

            GameTeam teamOne = playableTeams.get(0);
            GameTeam teamTwo = playableTeams.get(1);

            return teamOne.getDisplayName() + ": " + teamTwo.getAlivePlayers().size() + " - " + teamTwo.getDisplayName() + ": " + teamTwo.getAlivePlayers().size();
        }

        // Users level
        if (params.equals("level")) {
            return String.valueOf(user.getLevel());
        }

        // Coins earned through games
        if (params.equals("coins")) {
            return String.valueOf(user.getCoins());
        }

        // User lifetime kills
        if (params.equals("lifetime_kills")) {
            return String.valueOf(user.getLifetimeKills());
        }

        // User lifetime deaths
        if (params.equals("lifetime_deaths")) {
            return String.valueOf(user.getLifetimeDeaths());
        }

        return null; // Placeholder is unknown by the Expansion
    }
}
