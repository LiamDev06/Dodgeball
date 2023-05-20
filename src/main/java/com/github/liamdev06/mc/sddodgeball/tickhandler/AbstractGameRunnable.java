package com.github.liamdev06.mc.sddodgeball.tickhandler;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Used to create game runnabled.
 */
public abstract class AbstractGameRunnable {

    private final @NonNull String id;
    private final long ticks;
    private @Nullable BukkitTask task;

    public AbstractGameRunnable(@NonNull String id, long ticks) {
        this.id = id;
        this.ticks = ticks;
    }

    public abstract void run();

    public @NonNull String getId() {
        return this.id;
    }

    public @Nullable BukkitTask getTask() {
        return this.task;
    }

    public void setTask(@Nullable BukkitTask task) {
        this.task = task;
    }

    public long getTicks() {
        return this.ticks;
    }

    public @NonNull DodgeballPlugin getPlugin() {
        return DodgeballPlugin.getInstance();
    }
}
