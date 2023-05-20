package com.github.liamdev06.mc.sddodgeball.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Used to generate spawning locations for when spawning each team on the arena.
 */
public class PlayerSpawner {

    private static final @NonNull Random RANDOM = new Random();

    private final @NonNull String world;
    private final @NonNull Location positionOne;
    private final @NonNull Location positionTwo;
    private final @NonNull Set<Location> occupiedLocations;

    public PlayerSpawner(@NonNull String worldName, @NonNull Location positionOne, @NonNull Location positionTwo) {
        this.world = worldName;
        this.positionOne = positionOne;
        this.positionTwo = positionTwo;
        this.occupiedLocations = new HashSet<>();
    }

    public void spawnPlayers(@NonNull List<Player> players) {
        for (Player player : players) {
            Location spawnLocation;

            do {
                spawnLocation = this.generateRandomLocation();
            } while (!this.isLocationValid(spawnLocation));

            this.occupiedLocations.add(spawnLocation);
            player.teleport(spawnLocation);
        }
    }

    private Location generateRandomLocation() {
        double x = this.positionTwo.getX() + RANDOM.nextDouble() * (this.positionOne.getX() - this.positionTwo.getX());
        double z = this.positionTwo.getZ() + RANDOM.nextDouble() * (this.positionOne.getZ() - this.positionTwo.getZ());
        double y = this.positionOne.getY(); // Position one should always be the floor position and therefor use its Y

        return new Location(Bukkit.getWorld(this.world), x, y, z);
    }

    private boolean isLocationValid(@NonNull Location location) {
        for (Location occupiedLocation : this.occupiedLocations) {
            if (occupiedLocation.distanceSquared(location) < 1.0) {
                return false; // Locations are too close to each other
            }
        }

        return location.getBlock().getType().isAir(); // Check if the location is obstructed by a block
    }
}
