package com.github.liamdev06.mc.sddodgeball.storage.user.storage;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Stores Mongo database login credentials.
 *
 * @param ip Server ip.
 * @param port Server port.
 * @param user Mongo login user.
 * @param database The database.
 * @param password Mongo login password.
 * @param usersCollection The collection in the {@param database} to store users in.
 */
public record MongoCredentials(@NonNull String ip, @NonNull String port, @NonNull String user, @NonNull String database,
                               @NonNull String password, @NonNull String usersCollection) {

}
