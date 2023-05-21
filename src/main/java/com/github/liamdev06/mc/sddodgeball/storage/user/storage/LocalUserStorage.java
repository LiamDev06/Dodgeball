package com.github.liamdev06.mc.sddodgeball.storage.user.storage;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.storage.user.User;
import com.github.liamdev06.mc.sddodgeball.storage.GameFileStorage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages user storing with local storage within the
 * plugin data folder.
 */
public class LocalUserStorage implements IUserStorage {

    private final @NonNull Map<UUID, User> users;
    private final @NonNull GameFileStorage userStorage;

    public LocalUserStorage(@NonNull DodgeballPlugin plugin) {
        this.users = new HashMap<>();
        this.userStorage = plugin.getPlayerDataFileStorage();
    }

    @Override
    public CompletableFuture<User> getUser(@NonNull UUID uuid) {
        // Return user if they are in cache
        if (this.users.containsKey(uuid)) {
            return CompletableFuture.supplyAsync(() -> this.users.get(uuid), Runnable::run)
                    .exceptionally(exception -> {
                        exception.printStackTrace();
                        return null;
                    });
        }

        // Load in user from data.yml
        if (this.userStorage.contains("user_data." + uuid)) {
            return this.loadUserFromStorage(uuid);
        }

        // User does not exist
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Nullable
    public User getCachedUser(@NonNull UUID uuid) {
        return this.users.get(uuid);
    }

    @Override
    public CompletableFuture<Void> saveUserToStorage(@NonNull UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            if (!this.users.containsKey(uuid)) {
                return;
            }

            // Get user and file
            User user = this.users.get(uuid);

            FileConfiguration store = this.userStorage.configuration();
            String path = "user_data." + uuid + ".";

            // Store values
            for (String key : user.getValues().keySet()) {
                Object value = user.getValues().get(key);
                store.set(path + key, value);
            }

            // Save file
            try {
                store.save(this.userStorage.configurationFile());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }, Runnable::run);
    }

    @Override
    public CompletableFuture<User> loadUserFromStorage(@NonNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> values = new HashMap<>();
            String path = "user_data." + uuid;

            if (!this.userStorage.contains(path)) {
                return null;
            }

            // Load in values
            ConfigurationSection section = this.userStorage.getSection(path);
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    Object value = section.get(key);
                    values.put(key, value);
                }
            }

            // Create new user and save
            User user = new User(uuid);
            user.setValues(values);
            this.users.put(uuid, user);

            // Return user
            return user;
        }, Runnable::run).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<User> createNewUser(@NonNull UUID uuid) {
        String path = "user_data." + uuid;

        // Set default value
        this.userStorage.set(path, "");

        // Load in user
        return this.loadUserFromStorage(uuid);
    }

    @Override
    public void removeUserFromCache(@NonNull UUID uuid) {
        this.users.remove(uuid);
    }

    @Override
    @NonNull
    public Map<UUID, User> getUsers() {
        return this.users;
    }
}