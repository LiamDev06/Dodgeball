package com.github.liamdev06.mc.sddodgeball;

import com.github.liamdev06.mc.sddodgeball.game.BlockLocationPair;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameState;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameTeam;
import com.github.liamdev06.mc.sddodgeball.managers.world.WorldManager;
import com.github.liamdev06.mc.sddodgeball.tickhandler.RunnableManager;
import com.github.liamdev06.mc.sddodgeball.storage.user.storage.LocalUserStorage;
import com.github.liamdev06.mc.sddodgeball.storage.user.IUserStorage;
import com.github.liamdev06.mc.sddodgeball.storage.user.storage.MongoCredentials;
import com.github.liamdev06.mc.sddodgeball.storage.user.storage.MongoUserStorage;
import com.github.liamdev06.mc.sddodgeball.storage.GameFileStorage;
import com.github.liamdev06.mc.sddodgeball.managers.ScoreboardManager;
import com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegistry;
import com.github.liamdev06.mc.sddodgeball.utility.location.InvalidLocationParseException;
import com.github.liamdev06.mc.sddodgeball.utility.location.LocationHelper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Main plugin class. Handles start and shutdown logic of the plugin and
 * works as an access points for managers and other classes.
 */
public final class DodgeballPlugin extends JavaPlugin {

    private static DodgeballPlugin INSTANCE;
    private Map<String, Game> games;
    private FileConfiguration messagesConfig;
    private GameFileStorage pluginConfig;
    private GameFileStorage playerDataFileStorage;
    private GameFileStorage lobbyConfig;
    private GameFileStorage gameStorage;
    private IUserStorage userStorage;
    private RunnableManager runnableManager;
    private ScoreboardManager scoreboardManager;
    private WorldManager worldManager;

    @Override
    public void onEnable() {
        final long time = System.currentTimeMillis();
        Logger log = this.getLogger();
        log.info(this.getDescription().getName() + "  is starting up...");

        INSTANCE = this;
        PluginManager pluginManager = this.getServer().getPluginManager();

        // Checking if PlaceholderAPI is installed
        log.info("Hooking into PlaceholderAPI...");
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIExtension(this).register();
            log.info("Hooked extension into PlaceholderAPI!");
        } else {
            this.abort(pluginManager,
                    "*** PlaceholderAPI is not installed or not enabled! ***",
                    "*** The plugin will not be disabled, please install PlaceholderAPI! ***"
            );
        }

        if (this.setupConfigurations()) {
            log.info("Detected fresh plugin start-up! All files were created from default configurations.");
        }

        // Enable correct user storage
        String storageType = this.pluginConfig.getString("storage");
        if (storageType.equals("")) {
            storageType = "local";
        }

        if (storageType.equals("mongo")) {
            MongoCredentials credentials = new MongoCredentials(
                    this.pluginConfig.getString("mongo.ip"),
                    this.pluginConfig.getString("mongo.port"),
                    this.pluginConfig.getString("mongo.user"),
                    this.pluginConfig.getString("mongo.database"),
                    this.pluginConfig.getString("mongo.password"),
                    this.pluginConfig.getString("mongo.users-collection")
            );

            this.userStorage = new MongoUserStorage(this, credentials);
            log.info("Mongo storage was selected for user storage.");
        } else {
            this.userStorage = new LocalUserStorage(this);
            log.info("Local storage was selected for user storage.");
        }

        // Initialization
        this.scoreboardManager = new ScoreboardManager(this);
        this.runnableManager = new RunnableManager(this);
        this.runnableManager.registerRunnable();
        this.worldManager = new WorldManager(this);

        // Load in registered games
        this.games = new HashMap<>();
        ConfigurationSection gamesSection = this.gameStorage.getSection("registeredGames");
        if (gamesSection == null) {
            log.warning("The game_data.yml file is missing the 'registeredGames' section! Games could not be loaded in.");
        } else {
            for (String gameId : this.gameStorage.getSection("registeredGames").getKeys(false)) {
                Game game = this.loadInGame(gameId, gamesSection);
                if (game == null) {
                    continue;
                }

                this.games.put(gameId, game);
            }
        }

        int gamesSize = this.games.size();
        if (gamesSize == 0) {
            log.info("No games registered games exist within the game_data.yml. View the plugin documentation on how to set up a new game.");
        } else if (gamesSize == 1) {
            log.info("1 game was successfully registered.");
        } else {
            log.info(gamesSize + " games were registered.");
        }

        // Register commands and listeners
        log.info("Registering commands and listeners...");
        this.registerCommands();
        this.registerListeners(pluginManager);
        log.info("Commands and listeners were successfully registered!");

        // Start global default runnables
        this.runnableManager.start("scoreboard", false);
        this.runnableManager.start("timer", false);

        // Done
        log.info("Plugin has successfully loaded in " + (System.currentTimeMillis() - time) + "ms!");
    }

    @Override
    public void onDisable() {
        long time = System.currentTimeMillis();
        Logger log = this.getLogger();

        // Save all users
        if (this.userStorage == null) {
            log.severe("There is no instance of the UserManager class! Users could not be saved to the storage!");
        } else {
            this.userStorage.saveUsersToStorage();
            log.info("All users were saved to storage.");
        }

        // Save all games to storage
        if (this.games != null) {
            for (Game game : this.games.values()) {
                this.saveGameToStorage(game);
            }
        }

        this.gameStorage.save();

        // Shutdown user storing
        this.userStorage.handleShutdown();

        // Plugin shutdown done
        INSTANCE = null;
        log.info("The plugin was shutdown successfully shutdown in " + (System.currentTimeMillis() - time) + "ms!");
    }

    /**
     * Saves a game to storage by storing all
     * important and needed values of the game in the game_data.yml file.
     *
     * @param game The game to save.
     */
    private void saveGameToStorage(@NonNull Game game) {
        final String path = "registeredGames." + game.getGameId() + ".";
        this.gameStorage.setCache(path + "enabled", game.isEnabled());
        this.gameStorage.setCache(path + "worldName", game.getWorldName());
        this.gameStorage.setCache(path + "gameId", game.getGameId());
        this.gameStorage.setCache(path + "waitingLobbySpawn", LocationHelper.writeLocation(game.getWaitingLobbySpawn()));

        // Save teams
        for (GameTeam team : game.getPlayableTeams()) {
            final String teamPath = path + "team." + team.getId() + ".";
            this.gameStorage.setCache(teamPath + "displayName", team.getDisplayName());
            this.gameStorage.setCache(teamPath + "prefix", team.getPrefix());
            this.gameStorage.setCache(teamPath + "chatColor", team.getChatColor().name());
            this.gameStorage.setCache(teamPath + "playable", team.isPlayable());

            // Block location pair
            BlockLocationPair locationPair = team.getPlayableTeamArea();
            String positionOne = LocationHelper.writeLocation(locationPair.getPositionOne());
            String positionTwo = LocationHelper.writeLocation(locationPair.getPositionTwo());
            this.gameStorage.setCache(teamPath + "locationPair.one", positionOne);
            this.gameStorage.setCache(teamPath + "locationPair.two", positionTwo);
        }
    }

    /**
     * Loads in a game from a configuration section.
     *
     * @param gameId The id of the game to load in.
     * @param gamesSection The bukkit {@link FileConfiguration} to load it in from.
     * @return The new created instance of a game.
     */
    private @Nullable Game loadInGame(@NonNull String gameId, @NonNull ConfigurationSection gamesSection) {
        final String path = gameId + ".";
        Logger logger = this.getLogger();
        boolean enabled = gamesSection.getBoolean(path + "enabled");
        String worldName = gamesSection.getString(path + "worldName", "");

        // Create and load in the world
        this.worldManager.setupModifiedWorld(worldName);

        Location waitingLobbySpawn;
        try {
            waitingLobbySpawn = LocationHelper.parseLocation(gamesSection.getString(path + "waitingLobbySpawn", ""));
        } catch (InvalidLocationParseException exception) {
            exception.printStackTrace();
            logger.severe("The waiting lobby spawn location is broken in the game " + gameId);
            return null;
        }

        // Create game and apply settings
        Game game = new Game(this, gameId, worldName);
        game.setEnabled(enabled);
        game.setGameState(GameState.PRE_WAITING);
        game.setWaitingLobbySpawn(waitingLobbySpawn);

        // Load in teams
        for (String teamId : this.gameStorage.getSection("registeredGames." + gameId + ".team").getKeys(false)) {
            final String teamPath = path + "team." + teamId + ".";

            // Get team values
            String teamDisplayName = gamesSection.getString(teamPath + "displayName", "DISPLAYNAME_ERROR");
            String teamPrefix = gamesSection.getString(teamPath + "prefix", "PREFIX_ERROR");
            ChatColor teamChatColor = ChatColor.valueOf(gamesSection.getString(teamPath + "chatColor", "RED").toUpperCase());
            boolean playable = gamesSection.getBoolean(teamPath + "playable");

            // Block location pair
            Location positionOne;
            Location positionTwo;

            try {
                positionOne = LocationHelper.parseLocation(gamesSection.getString(teamPath + "locationPair.one", ""));
                positionTwo = LocationHelper.parseLocation(gamesSection.getString(teamPath + "locationPair.two", ""));
            } catch (InvalidLocationParseException exception) {
                logger.severe("The playable locations spawn locations are broken in the game " + gameId);
                return null;
            }

            // Create the team
            GameTeam team = new GameTeam(teamId, teamDisplayName, teamChatColor, teamPrefix, playable);
            BlockLocationPair locationPair = team.getPlayableTeamArea();
            locationPair.setPositionOne(positionOne);
            locationPair.setPositionTwo(positionTwo);

            // Add the team
            game.addTeam(team);
        }

        return game;
    }

    /**
     * Registers all command classes that are marked with the {@link com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegister}
     * annotation with the {@link AutoRegistry.Type#COMMAND} type.
     */
    private void registerCommands() {
        // Loop through all commands and init their constructor
        for (Class<?> clazz : AutoRegistry.getClassesWithRegisterType(AutoRegistry.Type.COMMAND)) {
            AutoRegistry.register(clazz, DodgeballPlugin.class, this);
        }
    }

    /**
     * Registers all listeners classes that are marked with the {@link com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegister}
     * annotation with the {@link AutoRegistry.Type#LISTENER} type .
     *
     * @param pluginManager Instance of the bukkit plugin manager {@link PluginManager}.
     */
    private void registerListeners(@NonNull PluginManager pluginManager) {
        for (Class<?> clazz : AutoRegistry.getClassesWithRegisterType(AutoRegistry.Type.LISTENER)) {
            // The target class contains no interfaces (which means no listener implemented), continue
            if (clazz.getInterfaces().length == 0) {
                continue;
            }

            try {
                // Initialize new constructor and cast to bukkit listener
                Listener listener = (Listener) clazz.getConstructor(DodgeballPlugin.class).newInstance(this);

                // Register the event using the regular method of PluginManager#registerEvents
                pluginManager.registerEvents(listener, this);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Sets up all the needed configurations for this plugin to properly work.
     * @return true if the setup is a fresh one meaning setting up all files from scratch.
     */
    private boolean setupConfigurations() {
        boolean freshSetup = false;

        // Check if we can detect a fresh setup
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            freshSetup = true;

           if (this.getDataFolder().mkdir()) {
               this.getLogger().info("Plugin data folder was created.");
           }
        }

        // Save config as a GameFileStorage
        configFile = this.setupFile("config.yml");
        this.pluginConfig = new GameFileStorage(configFile, YamlConfiguration.loadConfiguration(configFile));

        // Setting up messages.yml configuration
        File messagesFile = this.setupFile("messages.yml");
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Setting up player_data.yml store
        File playerDataFile = this.setupFile("player_data.yml");
        this.playerDataFileStorage = new GameFileStorage(playerDataFile, YamlConfiguration.loadConfiguration(playerDataFile));

        // Setting up game_data.yml
        File gameData = this.setupFile("game_data.yml");
        this.gameStorage = new GameFileStorage(gameData, YamlConfiguration.loadConfiguration(gameData));

        // Setting up lobby.yml
        File lobbyFile = this.setupFile("lobby.yml");
        this.lobbyConfig = new GameFileStorage(lobbyFile, YamlConfiguration.loadConfiguration(lobbyFile));

        return freshSetup;
    }

    /**
     * Create a new file in the Dodgeball plugin directory.
     *
     * @param fileName The name of the file, make sure to include the file extension.
     * @return The file that was just created (or if the file already exists, an instance of it).
     */
    private File setupFile(@NonNull String fileName) {
        // Create a new file and return if it exists
        File file = new File(this.getDataFolder(), fileName);
        if (file.exists()) {
            return file;
        }

        // Copy the file to the plugin data folder
        try (InputStream inputStream = this.getClassLoader().getResourceAsStream(fileName)) {
            // Create the file
            if (file.createNewFile() && inputStream != null) {
                // Copy the contents/file
                try (OutputStream outputStream = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return file;
    }

    /**
     * Abort the plugin by sending a series of log messages and disabling it.
     *
     * @param pluginManager Instance of the bukkit plugin manager {@link PluginManager}.
     * @param messages Array of warning messages to send when abort closing down.
     */
    private void abort(@NonNull PluginManager pluginManager, @NonNull String... messages) {
        Logger log = this.getLogger();

        // Log warnings
        for (String message : messages) {
            log.warning(message);
        }

        // Shut down plugin
        pluginManager.disablePlugin(this);
    }

    public static DodgeballPlugin getInstance() {
        return INSTANCE;
    }

    public IUserStorage getUserStorage() {
        return this.userStorage;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    public RunnableManager getRunnableManager() {
        return this.runnableManager;
    }

    public WorldManager getWorldManager() {
        return this.worldManager;
    }

    public FileConfiguration getMessagesConfig() {
        return this.messagesConfig;
    }

    /**
     * Instead of access #getConfig() directly, instead acces it
     * through the {@link DodgeballPlugin#getPluginConfig()}
     *
     * @see DodgeballPlugin#getPluginConfig()
     * @return null
     */
    @NotNull
    @Override
    @Deprecated
    public FileConfiguration getConfig() {
        return null;
    }

    public GameFileStorage getPluginConfig() {
        return this.pluginConfig;
    }

    public GameFileStorage getLobbyConfig() {
        return this.lobbyConfig;
    }

    public GameFileStorage getGameStore() {
        return this.gameStorage;
    }

    public GameFileStorage getPlayerDataFileStorage() {
        return this.playerDataFileStorage;
    }

    public Game getGameById(String id) {
        return this.games.get(id);
    }

    public void addGame(@NonNull Game game) {
        this.games.putIfAbsent(game.getGameId(), game);
    }

    public void removeGame(@NonNull String gameId) {
        this.games.remove(gameId);

        // Remove the game from the game store if it exists there
        this.gameStorage.set("registeredGames." + gameId, null);
    }

    public Map<String, Game> getGames() {
        return this.games;
    }
}