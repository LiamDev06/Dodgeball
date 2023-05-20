package com.github.liamdev06.mc.sddodgeball.commands.core;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Adds the ability to perform sub-commands in new classes. This organizes the
 * code more and keeps it more clean.
 */
public abstract class AbstractPlayerSubcommand {

    private final @NonNull String subcommandId;

    public AbstractPlayerSubcommand(@NonNull String subcommandId) {
        this.subcommandId = subcommandId;
    }

    public abstract void onPlayerSubcommand(@NonNull Player player, @NonNull String[] args);

    public @NonNull String getSubcommandId() {
        return this.subcommandId;
    }
}
