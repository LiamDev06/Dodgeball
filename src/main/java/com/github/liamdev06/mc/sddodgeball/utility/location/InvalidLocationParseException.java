package com.github.liamdev06.mc.sddodgeball.utility.location;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Thrown when the location being parsed in {@link LocationHelper#parseLocation(String)} is.
 * not valid and the method cannot return a {@link org.bukkit.Location}.
 */
public class InvalidLocationParseException extends Exception {

    /**
     * @param message The error message to send when this is thrown.
     */
    public InvalidLocationParseException(@NonNull String message) {
        super(message);
    }

}
