package com.github.liamdev06.mc.sddodgeball.listeners;

import com.github.liamdev06.mc.sddodgeball.DodgeballPlugin;
import com.github.liamdev06.mc.sddodgeball.api.events.GameStateChangeEvent;
import com.github.liamdev06.mc.sddodgeball.game.BlockLocationPair;
import com.github.liamdev06.mc.sddodgeball.game.Game;
import com.github.liamdev06.mc.sddodgeball.game.GamePlayer;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameState;
import com.github.liamdev06.mc.sddodgeball.game.enums.GameTeam;
import com.github.liamdev06.mc.sddodgeball.storage.GameFileStorage;
import com.github.liamdev06.mc.sddodgeball.game.GameHelper;
import com.github.liamdev06.mc.sddodgeball.storage.user.storage.IUserStorage;
import com.github.liamdev06.mc.sddodgeball.storage.user.User;
import com.github.liamdev06.mc.sddodgeball.utility.DefaultSound;
import com.github.liamdev06.mc.sddodgeball.game.PlayerSpawner;
import com.github.liamdev06.mc.sddodgeball.utility.SoundHelper;
import com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegister;
import com.github.liamdev06.mc.sddodgeball.utility.autoregistry.AutoRegistry;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.LibActionBar;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MessageHelper;
import com.github.liamdev06.mc.sddodgeball.utility.messaging.lang.MsgReplace;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Listener class with game specific listeners.
 */
@AutoRegister(type = AutoRegistry.Type.LISTENER)
public class GameListener implements Listener {

    private final @NonNull DodgeballPlugin plugin;
    private final @NonNull GameFileStorage config;
    private final @NonNull Map<UUID, Location> lastLocation;

    public GameListener(@NonNull DodgeballPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        this.lastLocation = new HashMap<>();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        // Get the game
        Game game = GameHelper.getGameFromPlayer(player);
        if (game == null) {
            return;
        }

        // Player left the game
        this.callGameLeaveActions(player, game);
    }

    @EventHandler
    public void onWorldChangeEvent(PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();

        // Check if the player joined a game
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            Game game = GameHelper.getGameFromPlayer(player);
            if (game == null) {
                return;
            }

            MsgReplace[] replacements = {
                    new MsgReplace("player", player.getName()),
                    new MsgReplace("players_joined", game.getPlayers().size())
            };
            World world = player.getWorld();
            List<Player> players = world.getPlayers();

            // The player left the game
            GameState gameState = game.getGameState();
            if (event.getFrom().getName().equals(game.getWorldName())) {
                this.callGameLeaveActions(player, game);
                return;
            }

            // Check if we can start the countdown timer
            int playersSize = game.getPlayers().size();
            if (gameState == GameState.PRE_WAITING && playersSize >= this.config.getInt("game.players-to-start-timer")) {
                game.setDelayedGameState(GameState.WAITING, 2);
            }

            // The player joined the game
            if (gameState.isWaiting()) {
                if (playersSize >= this.config.getInt("game.max-players")) {
                    game.setDelayedGameState(GameState.ACTIVE, 2);
                }

                for (Player target : players) {
                    MessageHelper.sendMessage(target, "join-game.announce-join", replacements);
                }
            }
            SoundHelper.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING);
        }, 4);
    }

    private void callGameLeaveActions(@NonNull Player player, @NonNull Game game) {
        MsgReplace[] replacements = {
                new MsgReplace("player", player.getName()),
                new MsgReplace("players_joined", game.getPlayers().size() - 1)
        };

        GamePlayer gamePlayer = game.getGamePlayer(player);
        gamePlayer.getTeam().removeAlivePlayer(player.getUniqueId());

        GameState gameState = game.getGameState();
        for (GamePlayer targetGamePlayer : game.getPlayers()) {
            Player targetPlayer = targetGamePlayer.toPlayer();
            if (targetPlayer == null) {
                continue;
            }

            if (gameState.isWaiting()) {
                MessageHelper.sendMessage(targetPlayer, "join-game.announce-quit", replacements);
            }
        }

        game.removePlayer(player);

        // Check if one team has no alive players left
        if (gameState == GameState.ACTIVE) {
            for (GameTeam team : game.getPlayableTeams()) {
                if (team.getAlivePlayers().size() == 0) {
                    game.setDelayedGameState(GameState.END, 3);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        final Game game = event.getAffectedGame();
        final GameState newState = event.getNewState();

        if (newState == GameState.ACTIVE) {
            this.callActiveActions(game);
        }

        if (newState == GameState.END) {
            this.callEndActions(game);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Snowball snowball) {
            if (snowball.getShooter() instanceof Player player) {
                // Get the game
                Game game = GameHelper.getGameFromPlayer(player);
                if (game == null) {
                    return;
                }

                // Get the game player
                GamePlayer gamePlayer = game.getGamePlayer(player);
                if (gamePlayer == null) {
                    return;
                }

                gamePlayer.incrementBallsThrown();
            }
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        final Item item = event.getItem();
        if (event.getEntity() instanceof Player player && item instanceof Snowball) {
            // Get the game
            Game game = GameHelper.getGameFromPlayer(player);
            if (game == null) {
                return;
            }

            game.removeSnowball(item);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball snowball && snowball.getShooter() instanceof Player shooter) {
            // Get the game
            Game game = GameHelper.getGameFromPlayer(shooter);
            if (game == null) {
                return;
            }

            // Check if the snowball hit a block
            Block hitBlock = event.getHitBlock();
            if (hitBlock != null) {
                ItemStack snowballItem = new ItemStack(Material.SNOWBALL, 1);

                // Drop the snowball where it landed and store it
                Item itemEntity = snowball.getWorld().dropItemNaturally(snowball.getLocation(), snowballItem);
                game.addSnowball(itemEntity);

                // Remove the snowball entity
                snowball.remove();
            }

            if (event.getHitEntity() instanceof Player hit) {
                // Get the game player
                GamePlayer gamePlayer = game.getGamePlayer(shooter);
                if (gamePlayer == null) {
                    return;
                }
                gamePlayer.incrementHits();

                // Notify players
                SoundHelper.playSound(shooter, Sound.BLOCK_NOTE_BLOCK_PLING);
                SoundHelper.playDefaultSound(hit, DefaultSound.ERROR);
                MessageHelper.sendMessage(shooter, "game.player-hit", new MsgReplace("hit", hit.getName()));
                MessageHelper.sendMessage(hit, "game.died", new MsgReplace("killer", shooter.getName()));

                // Lifetime statistics
                this.incrementDeath(hit);
                this.incrementKill(shooter);

                // Coins
                int coins = this.config.getInt("game.coins-on-kill");
                this.incrementCoins(shooter, coins);
                LibActionBar.sendActionBar(shooter, "&6+" + coins + " coins");

                // Check if there is any player left on the hit's team
                GamePlayer gamePlayerHit = game.getGamePlayer(hit);
                if (gamePlayerHit == null) {
                    return;
                }
                GameTeam hitTeam = gamePlayerHit.getTeam();
                hitTeam.removeAlivePlayer(gamePlayerHit.getUuid());
                hit.setGameMode(GameMode.SPECTATOR);
                gamePlayerHit.setTeamId("spectator");

                if (hitTeam.getAlivePlayers().size() == 0) {
                    game.setDelayedGameState(GameState.END, 3);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework firework) {
            // Do not cause damage if the firework is used as a victory effect
            if (firework.hasMetadata("victory_nodamage")) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Increment the stored coins for a player.
     *
     * @param player An online player to increment for.
     * @param coins The amount of coins to add on the current stored amount.
     */
    private void incrementCoins(@NonNull Player player, int coins) {
        IUserStorage userStorage = this.plugin.getUserStorage();
        User user = userStorage.getCachedUser(player.getUniqueId());

        if (user != null) {
            user.setCoins(user.getCoins() + coins);
        }
    }

    /**
     * Increment the amount of lifetime kills for a player.
     *
     * @param player An online player to increment for.
     */
    private void incrementKill(@NonNull Player player) {
        IUserStorage userStorage = this.plugin.getUserStorage();
        User user = userStorage.getCachedUser(player.getUniqueId());

        if (user != null) {
            user.setLifetimeKills(user.getLifetimeKills() + 1);
        }
    }

    /**
     * Increment the amount of lifetime deaths for a player.
     *
     * @param player An online player to increment for.
     */
    private void incrementDeath(@NonNull Player player) {
        IUserStorage userStorage = this.plugin.getUserStorage();
        User user = userStorage.getCachedUser(player.getUniqueId());

        if (user != null) {
            user.setLifetimeDeaths(user.getLifetimeDeaths() + 1);
        }
    }

    private void callEndActions(@NonNull Game game) {
        GameTeam winningTeam = null;

        for (GameTeam target : game.getPlayableTeams()) {
            int size = target.getAlivePlayers().size();

            if (size > 0) {
                winningTeam = target;
            }
        }

        if (winningTeam == null) {
            return;
        }

        // Send sound and title, and clear inventory
        List<GamePlayer> players = game.getPlayers();
        for (GamePlayer gamePlayer : players) {
            Player player = gamePlayer.toPlayer();
            if (player == null) {
                continue;
            }

            player.getInventory().clear();
            SoundHelper.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE);
            MessageHelper.sendTitle(player, "game.victory-title", "game.victory-subtitle", new MsgReplace("winning_team", winningTeam.getDisplayName()));
        }

        // Spawn visual effects
        for (UUID uuid : winningTeam.getAlivePlayers()) {
            Player target = Bukkit.getPlayer(uuid);
            if (target == null) {
                continue;
            }

            Location location = target.getLocation();
            this.spawnVictoryFireworks(location);
            this.showVictoryParticles(location);
        }

        // Send configuration commands
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        for (String command : this.config.getStringList("game.victory.commands")) {
            Bukkit.dispatchCommand(sender, command.replace("/", ""));
        }

        // Clean up snowballs
        for (Item snowball : game.getSnowballs()) {
            snowball.remove();
        }

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            for (GamePlayer gamePlayer : players) {
                Player player = gamePlayer.toPlayer();
                if (player == null) {
                    continue;
                }

                Location location = this.plugin.getLobbyConfig().getLocation("environment.lobby", false);
                if (location == null) {
                    player.kickPlayer("Game Over");
                } else {
                    player.teleport(location);
                }

                // Reset game options
                game.resetValues();
            }
        }, 20 * 6);
    }

    private void spawnVictoryFireworks(@NonNull Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        Firework firework = world.spawn(location, Firework.class);
        firework.setMetadata("victory_nodamage", new FixedMetadataValue(this.plugin, true)); // Used so the firework does not damage the player
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(FireworkEffect.builder()
                .withColor(Color.GREEN)
                .flicker(true)
                .build());
        firework.setFireworkMeta(fireworkMeta);

        // Schedule firework removal after 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                firework.detonate();
            }
        }.runTaskLater(this.plugin, 40);
    }

    private void showVictoryParticles(@NonNull Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        world.spawnParticle(org.bukkit.Particle.FIREWORKS_SPARK, location, 100, 1, 1, 1, 0.1);
    }

    private void callActiveActions(@NonNull Game game) {
        // Put players into teams
        final List<GamePlayer> players = game.getPlayers();
        int size = players.size();

        // Calculate the starting index for each team
        int teamOneIndex = 0;
        int teamTwoIndex = size / 2 + size % 2;
        Collections.shuffle(players);
        List<GameTeam> playableTeams = game.getPlayableTeams();
        GameTeam teamOne = playableTeams.get(0);
        GameTeam teamTwo = playableTeams.get(1);

        // Split the players into team and actually add them to a team
        for (int i = 0; i < players.size(); i++) {
            if (i % 2 == 0) {
                GamePlayer player = players.get(teamOneIndex++);
                player.setTeamId(teamOne.getId());
                teamOne.addAlivePlayer(player.getUuid());
            } else {
                GamePlayer player = players.get(teamTwoIndex++);
                player.setTeamId(teamTwo.getId());
                teamTwo.addAlivePlayer(player.getUuid());
            }
        }

        // Spawn the players
        for (GameTeam team : playableTeams) {
            BlockLocationPair playableTeamArea = team.getPlayableTeamArea();
            Location positionOne = playableTeamArea.getPositionOne();
            Location positionTwo = playableTeamArea.getPositionTwo();

            if (positionOne == null || positionTwo == null) {
                continue;
            }

            PlayerSpawner spawner = new PlayerSpawner(game.getWorldName(), positionOne.clone().add(0, 1.5, 0), positionTwo);

            List<Player> spawnPlayers = team.getAlivePlayers()
                    .stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            spawner.spawnPlayers(spawnPlayers);
        }

        // Visual effects to the players
        ItemStack snowball = new ItemStack(Material.SNOWBALL, this.config.getInt("game.snowballs-start-amount"));

        int teamOneSize = teamOne.getAlivePlayers().size();
        int teamTwoSize = teamTwo.getAlivePlayers().size();
        PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0);

        for (GamePlayer gamePlayer : players) {
            Player player = gamePlayer.toPlayer();
            if (player == null) {
                return;
            }

            // Team one is 1 less player than team two, give them a small speed boost as an extra perk
            String gameTeamId = gamePlayer.getTeam().getId();
            if (teamOneSize < teamTwoSize && gameTeamId.equals(teamOne.getId())) {
                player.addPotionEffect(speedEffect);
            } else if (teamTwoSize > teamOneSize && gameTeamId.equals(teamTwo.getId())) {
                // Team two is 1 less player than team one, give them a small speed boost as an extra perk
                player.addPotionEffect(speedEffect);
            }

            // Give snowballs
            player.getInventory().addItem(snowball);

            // Send message
            MessageHelper.sendMessage(player, "game.game-started.message");
            MessageHelper.sendTitle(player, "game.game-started.title", "game.game-started.subtitle");
            SoundHelper.playSound(player, Sound.ENTITY_ENDER_DRAGON_SHOOT);
            SoundHelper.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        // Get the game
        Game game = GameHelper.getGameFromPlayer(player);
        if (game == null) {
            return;
        }

        // Get the game player
        GamePlayer gamePlayer = game.getGamePlayer(player);
        if (gamePlayer == null) {
            return;
        }

        // Check if the player is trying to leave their team side
        GameTeam team = gamePlayer.getTeam();
        BlockLocationPair playableArea = team.getPlayableTeamArea();
        Location location = player.getLocation();
        Location positionOne = playableArea.getPositionOne();
        Location positionTwo = playableArea.getPositionTwo();

        // Player left the cuboid
        if (positionOne != null && positionTwo != null && !this.isInsideCuboid(location, positionOne, positionTwo)) {
            player.teleport(this.lastLocation.get(uuid));
            return;
        }

        // Store last location
        this.lastLocation.put(uuid, location);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (this.isGameWorld(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        World world = event.getWorld();

        // Stop weather changing in the game world
        for (Game game : this.plugin.getGames().values()) {
            if (game.getWorldName().equals(world.getName())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Checks if the base location can be found inside of the cuboid
     * generated by loc1 and loc2.
     *
     * @param base The base location, the location that controls the outcome of the boolean.
     * @param loc1 Position one to create the cuboid.
     * @param loc2 Position two to create the cuboid.
     * @return If {@param base} can be found inside of the cuboid created by {@param loc1} and {@param loc2}
     */
    private boolean isInsideCuboid(@NonNull Location base, @NonNull Location loc1, @NonNull Location loc2) {
        int x1 = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int y1 = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int z1 = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int x2 = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int y2 = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int z2 = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        return (base.getBlockX() >= x1 && base.getBlockX() <= x2 && base.getBlockY() >= y1 && base.getBlockY() <= y2 && base.getBlockZ() >= z1 && base.getBlockZ() <= z2);
    }

    /**
     * @param player An online player to check for.
     * @return If the player is in a game world.
     */
    private boolean isGameWorld(@NonNull Player player) {
        Game game = GameHelper.getGameFromPlayer(player);
        if (game == null) {
            return false;
        }

        return player.getWorld().getName().equalsIgnoreCase(game.getWorldName());
    }
}