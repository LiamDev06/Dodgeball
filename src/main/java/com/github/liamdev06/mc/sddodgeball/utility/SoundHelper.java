package com.github.liamdev06.mc.sddodgeball.utility;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

/**
 * Utility and helper class to play Bukkit sounds
 */
public class SoundHelper {

    /**
     *  Play a sound to a player at their location with a default volume a pitch.
     *
     * @param player An online player to play the sound for.
     * @param sound The Bukkit sound to play.
     */
    public static void playSound(@NonNull Player player, @NonNull Sound sound) {
        player.playSound(player.getLocation(), sound, 1, 1);
    }

    /**
     * Play a sound to a player at their location with a default volume a pitch,
     * but by providing their UUID.
     *
     * @param uuid The UUID of an online player to play the sound for.
     * @param sound The Bukkit sound to play.
     * @see SoundHelper#playDefaultSound(Player, DefaultSound)
     */
    public static void playSound(@NonNull UUID uuid, @NonNull Sound sound) {
        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            playSound(player, sound);
        }
    }

    /**
     * For quicker use, you can play a default sound with a default volume and pitch to
     * a player. All default sounds can be found in the {@link DefaultSound} enum.
     *
     * @param player An online player to play the sound for.
     * @param sound A default sound from the {@link DefaultSound} enum.
     */
    public static void playDefaultSound(@NonNull Player player, @NonNull DefaultSound sound) {
        player.playSound(player.getLocation(), sound.getSound(), (float) sound.getVolume(), (float) sound.getPitch());
    }
}
