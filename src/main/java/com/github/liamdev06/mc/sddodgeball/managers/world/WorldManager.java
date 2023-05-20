package com.github.liamdev06.mc.sddodgeball.managers.world;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import org.bukkit.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Handles creation and deletion of worlds.
 */
public class WorldManager {

    private final @NonNull DodgeballPlugin plugin;

    public WorldManager(@NotNull DodgeballPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a new world with a specified name and
     * sets certain default world settings to it.
     *
     * @param name The name of the world to create.
     * @return The world that was just created.
     */
    public World setupModifiedWorld(@NonNull String name) {
        // Create a world with the world creator
        new WorldCreator(name)
                .generator(new EmptyChunkGenerator())
                .createWorld();

        World world = Bukkit.getWorld(name);
        if (world == null) {
            return null;
        }

        // Apply default game settings to the world
        world.setAutoSave(true);
        world.setStorm(false);
        world.setWeatherDuration(0);
        world.setThunderDuration(0);
        world.setPVP(false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setTime(1500);
        return world;
    }

    /**
     * Deletes and unloads a world from the server.
     *
     * @param name The name of an existing world to unload and more.
     */
    public void deleteAndUnloadWorld(@NonNull String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            return;
        }

        Bukkit.getServer().unloadWorld(world, true);

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            this.deleteWorld(world.getWorldFolder());
        });
    }

    /**
     * Delete a world directory.
     *
     * @param path The path to the world directory.
     * @return If the directory was successfully deleted.
     */
    private boolean deleteWorld(@NonNull File path) {
        if (path.exists()) {
            File[] files = path.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteWorld(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }

        return path.delete();
    }
}
