package com.github.liamdev06.mc.sddodgeball.tickhandler.runnables;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.tickhandler.AbstractGameRunnable;
import com.github.liamdev06.mc.sddodgeball.storage.GameFileStorage;
import com.github.liamdev06.mc.sddodgeball.managers.ScoreboardManager;
import com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegister;
import com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegistry;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Runnable to update all scoreboard each 10 ticks.
 */
@AutoRegister(type = AutoRegistry.Type.RUNNABLE)
public class ScoreboardRunnable extends AbstractGameRunnable {

    private final @NonNull DodgeballPlugin plugin;

    public ScoreboardRunnable(@NonNull DodgeballPlugin plugin) {
        super("scoreboard", 10);
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ScoreboardManager scoreboardManager = this.plugin.getScoreboardManager();
        GameFileStorage configuration = this.plugin.getLobbyConfig();

        // Update game scoreboard
        for (Game game : this.plugin.getGames().values()) {
            if (game.getPlayers().size() > 0) {
                game.updateScoreboard();
            }
        }

        // Update lobby scoreboard
        Location location = configuration.getLocation("environment.lobby", false);
        if (location != null) {
            World world = location.getWorld();

            // Update the lobby scoreboard lines every tick if a player is in the lobby
            if (world != null && world.getPlayers().size() > 0) {
                scoreboardManager.updateLobbyScoreboards();
            }
        }
    }
}








