package com.github.liamdev06.mc.sddodgeball.utility;

import org.bukkit.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Enum to select default sounds.
 * Each sound has a Bukkit sound effect ({@link Sound}), as well as a volume and pitch value.
 */
public enum DefaultSound {

    SUCCESS_LIGHT(Sound.BLOCK_NOTE_BLOCK_PLING, 0.7, 1.1),
    SUCCESS_MAJOR(Sound.ENTITY_PLAYER_LEVELUP, 1, 1),
    CLICK(Sound.UI_BUTTON_CLICK, 1, 1),
    ERROR(Sound.ENTITY_ENDERMAN_TELEPORT, 1, -2),
    TIMER_BASS(Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);

    private final @NonNull Sound sound;
    private final double volume, pitch;

    DefaultSound(@NonNull Sound sound, double volume, double pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public @NonNull Sound getSound() {
        return this.sound;
    }

    public double getVolume() {
        return this.volume;
    }

    public double getPitch() {
        return this.pitch;
    }
}
