package com.github.liamdev06.mc.sddodgeball.tickhandler;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegistry;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores all runnables and have methods to
 * start and stop them whenever needed.
 */
public class RunnableManager {

    private final @NonNull DodgeballPlugin plugin;
    private final @NonNull Map<String, AbstractGameRunnable> runnables;

    public RunnableManager(@NonNull DodgeballPlugin plugin) {
        this.runnables = new HashMap<>();
        this.plugin = plugin;
    }

    public void registerRunnable() {
        try {
            for (Class<?> clazz : AutoRegistry.getClassesWithRegisterType(AutoRegistry.Type.RUNNABLE)) {
                this.addRunnable((AbstractGameRunnable) clazz.getConstructor(DodgeballPlugin.class).newInstance(this.plugin));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void start(@NonNull String id, boolean async) {
        if (this.runnables.containsKey(id)) {
            AbstractGameRunnable runnable = this.runnables.get(id);
            BukkitTask task;
            long ticks = runnable.getTicks();

            if (async) {
                task = new GameBukkitRunnable(runnable).runTaskTimerAsynchronously(this.plugin, ticks, ticks);
            } else {
                task = new GameBukkitRunnable(runnable).runTaskTimer(this.plugin, ticks, ticks);
            }

            runnable.setTask(task);
        }
    }

    public void stop(@NonNull String id) {
        if (!this.runnables.containsKey(id)) {
            return;
        }

        // Get runnable and bukkit task
        AbstractGameRunnable runnable = this.runnables.get(id);
        BukkitTask task = runnable.getTask();
        if (task == null) {
            return;
        }

        // Stop task
        runnable.getTask().cancel();
        runnable.setTask(null);
    }

    public void addRunnable(@NonNull AbstractGameRunnable runnable) {
        this.runnables.put(runnable.getId(), runnable);
    }

    public @Nullable AbstractGameRunnable getRunnable(@NonNull String id) {
        return this.runnables.get(id);
    }
}
