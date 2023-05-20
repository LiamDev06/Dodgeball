package com.github.liamdev06.mc.sddodgeball.storage.user;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to store temporary values for that do
 * not need to be saved if a player leaves the server.
 */
public class MetadataValues {

    private final @NonNull DodgeballPlugin plugin;
    private final @NonNull Map<String, Object> values;

    public MetadataValues(@NonNull DodgeballPlugin plugin) {
        this.values = new HashMap<>();
        this.plugin = plugin;
    }

    public void setGameInCreationMode(@NonNull Game game) {
        this.set("actualGameInCreationMode", game.getGameId());
    }

    public @Nullable Game getGameInCreationMode() {
        return this.plugin.getGameById(this.getString("actualGameInCreationMode"));
    }

    public void setInCreationMode(boolean value) {
        this.set("gameCreationMode", value);

        // Player left creation mode, reset the game ID value in game in creation mode
        if (!value) {
            this.set("actualGameInCreationMode", null);
        }
    }

    public boolean isInGameCreationMode() {
        return this.getBoolean("gameCreationMode");
    }

    public void set(@NonNull String key, Object value) {
        this.values.put(key, value);
    }

    public String getString(@NonNull String key) {
        Object object = this.get(key);
        return object == null ? "" : String.valueOf(object);
    }

    public int getInt(@NonNull String key) {
        Object object = this.get(key);
        return object == null ? 0 : Integer.parseInt(String.valueOf(object));
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

    private @Nullable Object get(@NonNull String key) {
        if (this.values.containsKey(key)) {
            return this.values.get(key);
        }

        return null;
    }

    public @NonNull Map<String, Object> getValues() {
        return this.values;
    }
}
