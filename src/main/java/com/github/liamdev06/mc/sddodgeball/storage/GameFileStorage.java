package com.github.liamdev06.mc.sddodgeball.storage;

import com.github.liamdev06.mc.sddodgeball.utility.location.LocationHelper;
import com.github.liamdev06.mc.sddodgeball.utility.location.InvalidLocationParseException;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Wraps the bukkit {@link FileConfiguration} in this game configuration class
 * that stores both the file and the actual configuration.
 */
public record GameFileStorage(File configurationFile, FileConfiguration configuration) {

    /**
     * Write changes to the configuration file.
     *
     * @param key The path to set the value at.
     * @param value The actual value to set.
     */
    public void set(@NonNull String key, Object value) {
        this.configuration.set(key, value);

        // Save information directly
        this.save();
    }

    /**
     * Set configuration keys without saving. Can be great when setting a lot of things at the same time, and
     * instead group the changes and save after
     *
     * @param key The path to set the value at.
     * @param value The actual value to set.
     */
    public void setCache(@NonNull String key, Object value) {
        this.configuration.set(key, value);
    }

    /**
     * Force save the configuration to the file.
     */
    public void save() {
        try {
            this.configuration.save(this.configurationFile);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Write changes to the configuration file async (writing in a new thread).
     * If you want to ensure does not affect gameplay, use this multithreaded option.
     *
     * @param key The path to set the value at.
     * @param value The actual value to set.
     * @see GameFileStorage#set(String, Object)
     */
    public CompletableFuture<Void> setAsync(@NonNull String key, Object value) {
        return CompletableFuture.runAsync(() -> this.set(key, value))
                .exceptionally(exception -> {
            exception.printStackTrace();
            return null;
        });
    }

    public ConfigurationSection getSection(@NonNull String key) {
        return this.configuration.getConfigurationSection(key);
    }

    /**
     * @param key The path to check for.
     * @return If the configuration contains a value at the {@param key} provided.
     */
    public boolean contains(@NonNull String key) {
        return this.configuration.contains(key);
    }

    public @Nullable Location getLocation(@NonNull String key, boolean logError) {
        try {
            return LocationHelper.parseLocation(this.getString(key));
        } catch (InvalidLocationParseException exception) {
            if (logError) {
                exception.printStackTrace();
            }
        }

        return null;
    }

    public @NonNull List<String> getStringList(@NonNull String key) {
        if (this.configuration.contains(key)) {
            return this.configuration.getStringList(key);
        }

        return Collections.emptyList();
    }

    public boolean ensureList(@NonNull String key) {
        return this.configuration.isList(key);
    }

    public String getString(@NonNull String key) {
        Object object = this.get(key);
        return object == null ? "" : String.valueOf(object);
    }

    public int getInt(@NonNull String key) {
        Object object = this.get(key);
        return object == null ? 0 : Integer.parseInt(String.valueOf(object));
    }

    public boolean ensureInt(@NonNull String key) {
        return this.configuration.isInt(key);
    }

    public short getShort(@NonNull String key) {
        Object object = this.get(key);
        return object == null ? 0 : Short.parseShort(String.valueOf(object));
    }

    public double getDouble(@NonNull String key) {
        Object object = this.get(key);
        return object == null ? 0 : Double.parseDouble(String.valueOf(object));
    }

    public float getFloat(@NonNull String key) {
        Object object = this.get(key);
        return object == null ? 0 : Float.parseFloat(String.valueOf(object));
    }

    public long getLong(@NonNull String key) {
        Object object = this.get(key);
        return object == null ? 0 : Long.parseLong(String.valueOf(object));
    }

    public boolean getBoolean(@NonNull String key) {
        Object object = this.get(key);
        return object != null && Boolean.parseBoolean(String.valueOf(object));
    }

    public boolean ensureBoolean(@NonNull String key) {
        return this.configuration.isBoolean(key);
    }

    private @Nullable Object get(@NonNull String key) {
        if (this.configuration.contains(key)) {
            return this.configuration.get(key);
        }

        return null;
    }
}
