package com.github.liamdev06.mc.sddodgeball.game;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * Stores custom add-on information for each player that is specific to the game.
 */
public class GamePlayer {

    private final @NonNull DodgeballPlugin plugin;
    private final @NonNull String gameId;
    private final @NonNull UUID uuid;
    private @NonNull String teamId;
    private int ballsThrown, hits;

    public GamePlayer(@NonNull String gameId, @NonNull UUID uuid, @NonNull String teamId) {
        this.gameId = gameId;
        this.plugin = DodgeballPlugin.getInstance();
        this.uuid = uuid;
        this.teamId = teamId;
        this.ballsThrown = 0;
        this.hits = 0;
    }

    public @NonNull GameTeam getTeam() {
        return this.getGame().getTeamById(this.teamId);
    }

    public GameTeam getOppositeTeam() {
        List<GameTeam> playableTeams = this.getGame().getPlayableTeams();

        for (GameTeam team : playableTeams) {
            if (!team.getId().equals(this.getTeam().getId())) {
                return team;
            }
        }

        return null;
    }

    /**
     * A set of bukkit values that when set back to default values, gives the
     * feeling of a reset player.
     */
    public void resetBukkitValues() {
        Player player = this.toPlayer();
        if (player == null) {
            return;
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setLevel(0);
        player.setExp(0);
        player.setHealthScale(20);
        player.setFoodLevel(20);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFlySpeed(.1f);
        player.setWalkSpeed(.2f);
        player.getInventory().clear();

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public void setTeamId(@NonNull String teamId) {
        this.teamId = teamId;
    }

    public int getBallsThrown() {
        return this.ballsThrown;
    }

    public void incrementBallsThrown() {
        this.ballsThrown++;
    }

    public int getHits() {
        return this.hits;
    }

    public void incrementHits() {
        this.hits++;
    }

    public @NonNull UUID getUuid() {
        return this.uuid;
    }

    public Game getGame() {
        return this.plugin.getGameById(this.gameId);
    }

    public Player toPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }
}
