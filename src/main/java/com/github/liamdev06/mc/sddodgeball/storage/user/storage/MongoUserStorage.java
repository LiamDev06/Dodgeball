package com.github.liamdev06.mc.sddodgeball.storage.user.storage;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.storage.user.User;
import com.github.liamdev06.mc.sddodgeball.storage.GameFileStorage;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages user storing using MongoDB.
 */
public class MongoUserStorage implements IUserStorage {

    private final @NonNull Map<UUID, User> users;
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> usersCollection;

    public MongoUserStorage(@NonNull DodgeballPlugin plugin, @NonNull MongoCredentials credentials) {
        this.users = new HashMap<>();
        GameFileStorage config = plugin.getPluginConfig();

        // Connect to the database
        String connectionUri = String.format("mongodb://%s:%s@%s:%s/%s?retryWrites=true&w=majority",
                credentials.user(),
                credentials.password(),
                credentials.ip(),
                credentials.port(),
                credentials.database());

        // Set forced connection string if applicable
        if (config.getBoolean("mongo.connection-string.use")) {
            connectionUri = config.getString("mongo.connection-string.forced-string")
                    .replace("{user}", credentials.user())
                    .replace("{password}", credentials.password())
                    .replace("{ip}", credentials.ip())
                    .replace("{database}", credentials.database());
        }

        ConnectionString connectionString = new ConnectionString(connectionUri);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applicationName("Dodgeball")
                .applyConnectionString(connectionString)
                .build();

        try {
            this.client = MongoClients.create(clientSettings);

            // Get the database
            this.database = this.client.getDatabase(credentials.database());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // Get the users collection
        String usersCollection = credentials.usersCollection();
        try {
            this.usersCollection = this.database.getCollection(usersCollection);
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("A collection with the name " + usersCollection + " does not exist! Creating one for you...");
            this.database.createCollection(usersCollection);
            this.usersCollection = this.database.getCollection(usersCollection);
        }
    }

    @Override
    public CompletableFuture<User> getUser(@NonNull UUID uuid) {
        if (this.users.containsKey(uuid)) {
            return CompletableFuture.supplyAsync(() -> this.users.get(uuid), Runnable::run)
                    .exceptionally(exception -> {
                        exception.printStackTrace();
                        return null;
                    });
        }

        // Load in user from the database
        return this.loadUserFromStorage(uuid);
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

            // Get user and their document
            User user = this.users.get(uuid);

            String sUuid = uuid.toString();
            Bson filter = Filters.eq("_id", sUuid);
            Document document = this.usersCollection.find(filter).first();

            boolean newDocument = false;
            if (document == null) {
                document = new Document();
                newDocument = true;
            }

            // Store values
            Map<String, Object> values = user.getValues();
            for (String key : values.keySet()) {
                Object value = values.get(key);

                if (document.containsKey(key)) {
                    document.replace(key, value);
                } else {
                    document.append(key, value);
                }
            }

            // Save the document
            if (newDocument) {
                this.usersCollection.insertOne(document);
            } else {
                this.usersCollection.findOneAndReplace(filter, document);
            }
        }).exceptionally(exception -> {
            exception.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<User> loadUserFromStorage(@NonNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sUuid = uuid.toString();

            // Check if user exists
            Document document = this.usersCollection.find(Filters.eq("_id", sUuid)).first();
            if (document == null) {
                return null;
            }

            // Retrieve values from the document
            Map<String, Object> values = new HashMap<>();
            for (Map.Entry<String, Object> entry : document.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                values.put(key, value);
            }

            // Create new user and save
            User user = new User(uuid);
            user.setValues(values);
            this.users.put(uuid, user);

            // Return the user
            return user;
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<User> createNewUser(@NonNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sUuid = uuid.toString();
            Document document = new Document("_id", sUuid);

            // Save document to the collection
            this.usersCollection.insertOne(document);

            // Load in the user
            try {
                return this.loadUserFromStorage(uuid).get();
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            return null;
        }).exceptionally(exception -> {
            exception.printStackTrace();
            return null;
        });
    }

    @Override
    public void removeUserFromCache(@NonNull UUID uuid) {
        this.users.remove(uuid);
    }

    @Override
    public void handleShutdown() {
        this.client.close();
    }

    @Override
    @NonNull
    public Map<UUID, User> getUsers() {
        return this.users;
    }
}