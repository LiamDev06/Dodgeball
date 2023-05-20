package com.github.liamdev06.mc.sddodgeball.managers;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.storage.GameFileStorage;
import com.github.liamdev06.mc.sddodgeball.storage.user.User;
import com.github.liamdev06.mc.sddodgeball.utility.fastboard.FastBoard;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.LibColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

/**
 * Manages the scoreboard in the main lobby.
 */
public class ScoreboardManager {

    private static final @NonNull String LOBBY_SCOREBOARD_PATH = "scoreboard.";

    private final @NonNull DodgeballPlugin plugin;
    private final @NonNull GameFileStorage lobbyConfig;
    private final @NonNull Map<UUID, FastBoard> lobbyScoreboards;
    private final List<String> lobbyScoreboardLines;

    public ScoreboardManager(@NonNull DodgeballPlugin plugin) {
        this.plugin = plugin;
        this.lobbyConfig = plugin.getLobbyConfig();
        this.lobbyScoreboards = new HashMap<>();
        this.lobbyScoreboardLines = new ArrayList<>();

        // Load in lobby scoreboard lines
        String scoreboardPath = LOBBY_SCOREBOARD_PATH + "lines";
        if (!this.lobbyConfig.ensureList(scoreboardPath)) {
            plugin.getLogger().warning("The scoreboard lines in lobby.yml at " + scoreboardPath + " are not valid!");
        }

        for (String line : this.lobbyConfig.getStringList(scoreboardPath)) {
            this.lobbyScoreboardLines.add(LibColor.colorMessage(line));
        }
    }

    /**
     * "Spawn in" a new lobby scoreboard for a specified player
     * using {@link FastBoard}.
     *
     * @param player An online player to spawn the scoreboard for.
     */
    public void applyLobbyScoreboard(@NonNull Player player) {
        // Create a new scoreboard using FastBoard
        FastBoard scoreboard = new FastBoard(player);

        // Set the title
        String scoreboardTitle = LibColor.colorMessage(this.lobbyConfig.getString(LOBBY_SCOREBOARD_PATH + "title"));
        scoreboard.updateTitle(scoreboardTitle);
        this.updateLobbyScoreboard(scoreboard);

        // Store the scoreboard with the player
        this.lobbyScoreboards.putIfAbsent(player.getUniqueId(), scoreboard);
    }

    /**
     * Update the lobby scoreboard of a provided {@link FastBoard}.
     *
     * @param board The board to update for.
     */
    public void updateLobbyScoreboard(@NonNull FastBoard board) {
        Player player = board.getPlayer();
        List<String> lines = new ArrayList<>(this.lobbyScoreboardLines);

        for (int i = 0; i < lines.size(); i++) {
            String content = lines.get(i);
            content = this.applyReplacements(player, content);
            lines.set(i, content);
        }

        board.updateLines(lines);
    }

    /**
     * Update lobby scoreboards for all cached scoreboards.
     */
    public void updateLobbyScoreboards() {
        for (FastBoard board : this.lobbyScoreboards.values()) {
            this.updateLobbyScoreboard(board);
        }
    }

    /**
     * Apply message replacements to an input string.
     *
     * @param player An online player to get data values from.
     * @param input The input string to perform replacements in.
     * @return The formatted replaced string.
     */
    private String applyReplacements(@NonNull Player player, @NonNull String input) {
        User user = this.plugin.getUserStorage().getCachedUser(player.getUniqueId());
        if (user == null) {
            return input;
        }

        return input.replace("{level}", String.valueOf(user.getLevel()))
                .replace("{coins}", String.valueOf(user.getCoins()))
                .replace("{lifetime_kills}", String.valueOf(user.getLifetimeKills()))
                .replace("{lifetime_deaths}", String.valueOf(user.getLifetimeDeaths()));
    }

    /**
     * Clears the lobby scoreboard from a player's view.
     *
     * @param player An online player to clear the scoreboard for.
     */
    public void clearLobbyScoreboard(@NonNull Player player) {
        UUID uuid = player.getUniqueId();
        if (this.lobbyScoreboards.containsKey(uuid)) {
            FastBoard scoreboard = this.lobbyScoreboards.get(uuid);

            if (!scoreboard.isDeleted()) {
                scoreboard.delete();
            }

            this.lobbyScoreboards.remove(uuid);
        }
    }

    /**
     * @return If the lobby scoreboard is enabled in the lobby confiruation.
     */
    public boolean isLobbyScoreboardEnabled() {
        // Check if the "enable scoreboard" is a boolean
        if (!this.lobbyConfig.ensureBoolean(LOBBY_SCOREBOARD_PATH + "enable-scoreboard")) {
            return false;
        }

        return this.lobbyConfig.getBoolean(LOBBY_SCOREBOARD_PATH + "enable-scoreboard");
    }
}
