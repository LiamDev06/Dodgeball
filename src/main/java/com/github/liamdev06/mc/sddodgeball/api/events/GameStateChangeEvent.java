package com.github.liamdev06.mc.sddodgeball.api.events;

import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Even that is triggered whenever the game state in a game is changed.
 */
public class GameStateChangeEvent extends Event implements Cancellable {

    private final @NonNull GameState oldState;
    private final @NonNull GameState newState;
    private final @NonNull Game affectedGame;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;

    public GameStateChangeEvent(@NonNull GameState oldState, @NonNull GameState newState, @NonNull Game affectedGame) {
        this.oldState = oldState;
        this.newState = newState;
        this.affectedGame = affectedGame;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public @NonNull Game getAffectedGame() {
        return this.affectedGame;
    }

    public @NonNull GameState getNewState() {
        return this.newState;
    }

    public @NonNull GameState getOldState() {
        return this.oldState;
    }
}












