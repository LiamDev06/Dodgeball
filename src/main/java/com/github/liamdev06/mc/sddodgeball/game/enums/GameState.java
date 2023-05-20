package com.github.liamdev06.mc.sddodgeball.game.enums;

/**
 * Allows to store which current state a game is in.
 */
public enum GameState {

    /**
     * When the game is being setup and not is not possible to join.
     * For example when the map is being loaded in and setup.
     */
    SETUP,

    /**
     * The waiting before the waiting timer starts.
     */
    PRE_WAITING,

    /**
     * The waiting stage when the waiting timer is active.
     */
    WAITING,

    /**
     * When the game is active and being played.
     */
    ACTIVE,

    /**
     * When a game is over and complete and a team has won.
     */
    END;

    public boolean isWaiting() {
        return this == WAITING || this == PRE_WAITING;
    }
}
