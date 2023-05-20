package com.github.liamdev06.mc.sddodgeball.game.enums;

import com.github.liamdev06.mc.sddodgeball.game.BlockLocationPair;
import org.bukkit.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stores information about a team in the game.
 */
public class GameTeam {

    private final @NonNull String id, displayName, prefix;
    private final @NonNull ChatColor chatColor;
    private final @NonNull BlockLocationPair playableTeamArea;
    private final boolean playable;
    private final @NonNull List<UUID> alivePlayers;

    public GameTeam(@NonNull String id, @NonNull String displayName,@NonNull ChatColor chatColor, @NonNull String prefix, boolean playable) {
        this.id = id;
        this.displayName = displayName;
        this.chatColor = chatColor;
        this.prefix = prefix;
        this.playableTeamArea = new BlockLocationPair();
        this.playable = playable;
        this.alivePlayers = new ArrayList<>();
    }

    public @NonNull String getId() {
        return this.id;
    }

    public @NonNull String getDisplayName() {
        return this.displayName;
    }

    public @NonNull ChatColor getChatColor() {
        return this.chatColor;
    }

    public @NonNull String getPrefix() {
        return this.prefix;
    }

    public @NonNull BlockLocationPair getPlayableTeamArea() {
        return this.playableTeamArea;
    }

    public boolean isPlayable() {
        return this.playable;
    }

    public @NonNull List<UUID> getAlivePlayers() {
        return this.alivePlayers;
    }

    public void addAlivePlayer(@NonNull UUID uuid) {
        this.alivePlayers.add(uuid);
    }

    public void removeAlivePlayer(@NonNull UUID uuid) {
        this.alivePlayers.remove(uuid);
    }
}
