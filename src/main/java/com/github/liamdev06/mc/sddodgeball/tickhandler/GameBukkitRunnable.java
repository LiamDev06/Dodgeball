package com.github.liamdev06.mc.sddodgeball.tickhandler;

import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Performs the bukkit end of what is needed in the runnable
 * framework to run tasks.
 */
public class GameBukkitRunnable extends BukkitRunnable {

    private final @NonNull AbstractGameRunnable runnable;

    public GameBukkitRunnable(@NonNull AbstractGameRunnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        this.runnable.run();
    }
}
