package com.github.liamdev06.mc.sddodgeball.storage.user;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IUserStorage {

    CompletableFuture<User> getUser(@NonNull UUID uuid);

    /**
     * Should ONLY be used if you are absolutely certain that the player has been cached.
     * The player is cached upon server join, but it will always be safer to use {@link IUserStorage#getUser(UUID)}
     *
     * @param uuid The UUID of the player to get the user object for.
     * @return Instance of the user for the specified player.
     */
    @Nullable User getCachedUser(@NonNull UUID uuid);

    void saveUserToStorage(@NonNull UUID uuid);

    void saveUsersToStorage();

    CompletableFuture<User> loadUserFromStorage(@NonNull UUID uuid);

    CompletableFuture<User> createNewUser(@NonNull UUID uuid);

    void removeUserFromCache(@NonNull UUID uuid);

    @NonNull Map<UUID, User> getUsers();

    default void handleShutdown() {}
}
