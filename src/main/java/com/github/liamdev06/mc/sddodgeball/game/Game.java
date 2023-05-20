package com.github.liamdev06.mc.sddodgeball.game;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.api.events.GameStateChangeEvent;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameState;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameTeam;
import com.github.liamdev06.mc.sddodgeball.storage.GameFileStorage;
import com.github.liamdev06.mc.sddodgeball.utility.fastboard.FastBoard;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.LibColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main game class. Stores information about each game.
 * Each instance of this class is its own game.
 */
public class Game {

    private final @NonNull DodgeballPlugin plugin;
    private final @NonNull GameFileStorage config;
    private boolean enabled;
    private final @NonNull Set<GameTeam> teams;
    private final @NonNull List<GamePlayer> players;
    private final @NonNull Map<UUID, FastBoard> fastBoards;
    private final @NonNull List<Item> snowballs;
    private @NonNull GameState gameState;
    private final @NonNull String worldName, gameId;
    private Location waitingLobbySpawn;
    private int waitingCountdown;

    public Game(@NonNull DodgeballPlugin plugin, @NonNull String gameId, @NonNull String worldName) {
        this.plugin = plugin;
        this.enabled = false;
        this.gameId = gameId;
        this.config = plugin.getPluginConfig();
        this.fastBoards = new HashMap<>();
        this.teams = new HashSet<>();
        this.players = new ArrayList<>();
        this.gameState = GameState.SETUP;
        this.worldName = worldName;
        this.waitingCountdown = this.config.getInt("game.waiting-timer");
        this.snowballs = new ArrayList<>();

        // Create a new team for spectators and none
        this.teams.add(new GameTeam("none", "N/A", ChatColor.GRAY, "&7&lN/A", false));
        this.teams.add(new GameTeam("spectator", "Spectator", ChatColor.GRAY, "&2&lSpec", false));
    }

    public @NonNull String getGameId() {
        return this.gameId;
    }

    public void sendPlayer(@NonNull Player player) {
        UUID uuid = player.getUniqueId();

        // Create and add the player
        GamePlayer gamePlayer = new GamePlayer(this.gameId, uuid, "none");
        this.players.add(gamePlayer);

        // Create a new scoreboard sidebar if the player does not have it
        if (!this.fastBoards.containsKey(uuid)) {
            FastBoard fastBoard = new FastBoard(player);

            // Set the title
            String scoreboardTitle = this.config.getString("game.scoreboard.title");
            fastBoard.updateTitle(LibColor.colorMessage(scoreboardTitle));
            this.fastBoards.put(uuid, fastBoard);
        }

        // Teleport the player to the waiting spawn
        player.teleport(this.waitingLobbySpawn);

        // Reset values
        gamePlayer.resetBukkitValues();
    }

    public void removePlayer(@NonNull Player player) {
        UUID uuid = player.getUniqueId();

        // Get the game player and remove
        GamePlayer gamePlayer = this.getGamePlayer(player);
        this.players.remove(gamePlayer);
        gamePlayer.resetBukkitValues();

        // Clear scoreboard
        if (this.fastBoards.containsKey(uuid)) {
            FastBoard fastBoard = this.fastBoards.get(uuid);
            fastBoard.delete();

            this.fastBoards.remove(uuid);
        }

        // Check if go back to pre-waiting
        if (this.gameState == GameState.WAITING && this.players.size() < this.config.getInt("game.players-to-start-timer")) {
            this.resetValues();
        }
    }

    public void updateScoreboard() {
        for (FastBoard board : this.fastBoards.values()) {
            GamePlayer gamePlayer = this.getGamePlayer(board.getPlayer());
            if (gamePlayer == null) {
                continue;
            }

            List<String> lines;

            // Update scoreboard lines for waiting stage
            if (this.gameState.isWaiting()) {
                lines = new ArrayList<>(this.config.getStringList("game.scoreboard.waiting-lines"));

                for (int i = 0; i < lines.size(); i++) {
                    String content = lines.get(i);
                    content = LibColor.colorMessage(this.applyWaitingLinesReplacements(content));
                    lines.set(i, content);
                }
            } else {
                // Update scoreboard lines for general stages
                lines = new ArrayList<>(this.config.getStringList("game.scoreboard.game-lines"));

                for (int i = 0; i < lines.size(); i++) {
                    String content = lines.get(i);
                    content = LibColor.colorMessage(this.applyWaitingLinesReplacements(gamePlayer, content));
                    lines.set(i, content);
                }
            }

            board.updateLines(lines);
        }
    }

    /**
     * Applies message replacements for the waiting scoreboard lines.
     *
     * @param input Input line.
     * @return The input with replacements.
     */
    private String applyWaitingLinesReplacements(@NonNull String input) {
        return input.replace("{players}", String.valueOf(this.players.size()));
    }

    /**
     * Applies message replacements for the game scoreboard lines.
     *
     * @param input Input line.
     * @return The input with replacements.
     */
    private String applyWaitingLinesReplacements(@NonNull GamePlayer gamePlayer, @NonNull String input) {
        GameTeam team = gamePlayer.getTeam();
        GameTeam oppositeTeam = gamePlayer.getOppositeTeam();

        input = input.replace("{balls_thrown}", String.valueOf(gamePlayer.getBallsThrown()))
                .replace("{hits}", String.valueOf(gamePlayer.getHits()))
                .replace("{team_left}", String.valueOf(team.getAlivePlayers().size()))
                .replace("{team_prefix}", team.getPrefix())
                .replace("{team_display_name}", team.getDisplayName())
                .replace("{team_color}", team.getChatColor() + "");

        if (oppositeTeam == null) {
            return input;
        } else {
            return input.replace("{opposite_left}", String.valueOf(oppositeTeam.getAlivePlayers().size()))
                    .replace("{opposite_prefix}", oppositeTeam.getPrefix())
                    .replace("{opposite_display_name}", oppositeTeam.getDisplayName())
                    .replace("{opposite_color}", oppositeTeam.getChatColor() + "");
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addTeam(@NonNull GameTeam team) {
        if (this.getTeamById(team.getId()) != null) {
            return;
        }

        this.teams.add(team);
    }

    public void removeTeam(@NonNull String teamId) {
        this.teams.removeIf(team -> team.getId().equals(teamId));
    }

    public void setGameState(@NonNull GameState gameState) {
        this.actualSetGameState(gameState);
    }

    public void setDelayedGameState(@NonNull GameState gameState, long delay) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.actualSetGameState(gameState), delay);
    }

    private void actualSetGameState(@NonNull GameState gameState) {
        GameStateChangeEvent event = new GameStateChangeEvent(this.gameState, gameState, this);
        this.plugin.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            this.gameState = gameState;
        }
    }

    public @NonNull GameState getGameState() {
        return this.gameState;
    }

    public GameTeam getTeamById(@NonNull String id) {
        for (GameTeam team : this.teams) {
            if (team.getId().equals(id)) {
                return team;
            }
        }

        return null;
    }

    public @NonNull Set<GameTeam> getTeams() {
        return this.teams;
    }

    public @NonNull List<GameTeam> getPlayableTeams() {
        return this.teams.stream()
                .filter(GameTeam::isPlayable)
                .collect(Collectors.toList());
    }

    public boolean inGame(@NonNull UUID uuid) {
        for (GamePlayer gamePlayer : this.players) {
            if (gamePlayer.getUuid() == uuid) {
                return true;
            }
        }

        return false;
    }

    public GamePlayer getGamePlayer(@NonNull Player player) {
        UUID uuid = player.getUniqueId();

        for (GamePlayer gamePlayer : this.players) {
            if (gamePlayer.getUuid() == uuid) {
                return gamePlayer;
            }
        }

        return null;
    }

    public @NonNull List<GamePlayer> getPlayers() {
        return this.players;
    }

    public void setWaitingLobbySpawn(@NonNull Location waitingLobbySpawn) {
        this.waitingLobbySpawn = waitingLobbySpawn;
    }

    public Location getWaitingLobbySpawn() {
        return this.waitingLobbySpawn;
    }

    public int getWaitingCountdown() {
        return this.waitingCountdown;
    }

    public void decreaseWaitingCountdown() {
        this.waitingCountdown--;
    }

    public void resetWaitingCountdown() {
        this.waitingCountdown = this.config.getInt("game.waiting-timer");
    }

    public void resetValues() {
        this.resetWaitingCountdown();
        this.setDelayedGameState(GameState.PRE_WAITING, 3);
        this.clearSnowballs();
    }

    public void clearSnowballs() {
        this.snowballs.clear();
    }

    public @NonNull String getWorldName() {
        return this.worldName;
    }

    public void addSnowball(@NonNull Item item) {
        this.snowballs.add(item);
    }

    public void removeSnowball(@NonNull Item item) {
        this.snowballs.remove(item);
    }

    public @NonNull List<Item> getSnowballs() {
        return this.snowballs;
    }

    public @NonNull DodgeballPlugin getPlugin() {
        return this.plugin;
    }
}