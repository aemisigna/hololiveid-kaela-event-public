package com.covercorp.kaelaevent.minigame.games.zombie.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.zombie.ZombieMiniGame;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.bar.ZombieTimeBarHelper;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.listener.ZombieMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.listener.ZombieMatchListener;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.properties.ZombieMatchProperties;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.scoreboard.ZombieScoreboardHelper;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.spot.EntitySpot;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.state.ZombieMatchState;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.task.ZombieFireworkTask;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.task.ZombiePreLobbyTask;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.task.ZombieTickTask;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.task.ZombieTimeTask;
import com.covercorp.kaelaevent.minigame.games.zombie.inventory.ZombieItemCollection;
import com.covercorp.kaelaevent.minigame.games.zombie.player.ZombiePlayer;
import com.covercorp.kaelaevent.minigame.games.zombie.team.ZombieTeam;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import com.covercorp.kaelaevent.util.ZoneCuboid;
import com.covercorp.kaelaevent.util.simple.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
public final class ZombieArena {
    private final ZombieMiniGame zombieMiniGame;

    private final PlayerHelper<ZombieMiniGame> playerHelper;
    private final TeamHelper<ZombieMiniGame, ZombiePlayer> teamHelper;
    private final Announcer<ZombieMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final ZombieScoreboardHelper zombieScoreboardHelper;

    private final Location lobbyLocation;
    private final Location arenaSpawnLocation;
    private final ZoneCuboid shootZone;

    private final ZombieTimeBarHelper zombieTimeBarHelper;

    private final ZombieMatchProperties zombieMatchProperties;

    private final int timeLimit;
    private final static int DEFAULT_TIME_LIMIT = 90; //60 * 2;

    @Setter(AccessLevel.PUBLIC) private int gameTime;
    @Setter(AccessLevel.PUBLIC) private ZombieMatchState state;

    private final Map<UUID, EntitySpot> zombies;

    private ZombieTeam winnerTeam;

    public ZombieArena(final ZombieMiniGame zombieMiniGame) {
        this.zombieMiniGame = zombieMiniGame;

        playerHelper = zombieMiniGame.getPlayerHelper();
        teamHelper = zombieMiniGame.getTeamHelper();
        announcer = zombieMiniGame.getAnnouncer();

        gameMiniMessage = zombieMiniGame.getMiniMessage();

        zombieScoreboardHelper = new ZombieScoreboardHelper(this);

        lobbyLocation = zombieMiniGame.getConfigHelper().getLobbySpawn();
        arenaSpawnLocation = zombieMiniGame.getConfigHelper().getArenaSpawn();
        shootZone = zombieMiniGame.getConfigHelper().getShootZone();

        zombieTimeBarHelper = new ZombieTimeBarHelper(this);

        zombieMatchProperties = new ZombieMatchProperties(this);

        timeLimit = DEFAULT_TIME_LIMIT;

        zombies = new ConcurrentHashMap<>();
        zombieMiniGame.getConfigHelper().getZombieSpawns().forEach(zombieSpawnLocId -> {
            final UUID uuid = UUID.randomUUID();
            final Pair<Location, Location> pointPair = zombieMiniGame.getConfigHelper().getZombieSpawn(zombieSpawnLocId);
            final EntitySpot spot = new EntitySpot(this, uuid, pointPair.key(), pointPair.value());

            zombies.put(uuid, spot);
        });
        
        winnerTeam = null;

        clearZombies();
        
        Bukkit.getServer().getPluginManager().registerEvents(new ZombieMatchListener(this), getZombieMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new ZombieMatchGameListener(this), getZombieMiniGame().getKaelaEvent());

        setState(ZombieMatchState.WAITING);
    }

    public void start() {
        // Cancel the match start item task
        Bukkit.getScheduler().cancelTask(getZombieMatchProperties().getStartingTaskId());
        zombieMatchProperties.setStartingTaskId(0);

        clearZombies();

        setGameTime(0);

        this.playerHelper.getPlayerList().forEach(participant -> {
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) {
                return;
            }
            final ZombieTeam team = (ZombieTeam)participant.getMiniGameTeam();
            if (team == null) {
                return;
            }
            player.teleport(this.getArenaSpawnLocation());
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setFlying(false);
        });

        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>ZOMBIE RANGE"),
                gameMiniMessage.deserialize("<gray>Get ready!"),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(1000)
                )
        ));

        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);

        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lThe Zombie Range", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lShoot the zombies!", true);
        announcer.sendGlobalMessage("&e&lUse your bow to shoot the running zombies", true);
        announcer.sendGlobalMessage("&e&lin front of you.", true);
        announcer.sendGlobalMessage("&e&lThe talent with most score wins!", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        // The task constructor has the setPreLobby(true) func
        zombieMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(zombieMiniGame.getKaelaEvent(), new ZombiePreLobbyTask(this), 0L, 20L).getTaskId()
        );
        setState(ZombieMatchState.ARENA_STARTING);
    }

    public void postStart() {
        // Cancel the previous task
        zombieMatchProperties.setPreLobby(false);
        Bukkit.getScheduler().cancelTask(zombieMatchProperties.getPreLobbyTaskId());

        zombieMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getZombieMiniGame().getKaelaEvent(), new ZombieTickTask(this), 0L, 1L).getTaskId()
        );
        zombieMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getZombieMiniGame().getKaelaEvent(), new ZombieTimeTask(this), 0L, 20L).getTaskId()
        );

        zombieTimeBarHelper.start();

        announcer.sendGlobalMessage("&eShoot!", false);
        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>START!"),
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(1000),
                        Duration.ofMillis(500)
                )
        ));
        announcer.sendGlobalSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0F, 2.0F);

        spawnZombies();

        setState(ZombieMatchState.GAME);
    }

    public void stop() {
        setState(ZombieMatchState.ENDING);

        stopTasks();
        zombieTimeBarHelper.stop();
        clearZombies();

        if (winnerTeam == null) {
            announcer.sendGlobalMessage(" \n&6&lThe game ended without a winner... let's try again!", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);
        } else {
            announcer.sendGlobalTitle(Title.title(gameMiniMessage.deserialize(
                    "<green>Game ended!"),
                    getWinnerTeam().getBetterPrefix()
                            .append(gameMiniMessage.deserialize("<gray>won the match!")),
                    Title.Times.times(Duration.ofMillis(0L), Duration.ofMillis(5000L), Duration.ofMillis(1000L)))
            );

            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
            announcer.sendGlobalMessage("&e&lMatch ended!", true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&eWinner team: &f" + LegacyComponentSerializer.legacyAmpersand().serialize(getWinnerTeam().getBetterPrefix()), true);
            announcer.sendGlobalMessage(getWinnerTeam().getPlayers().stream().map(ZombiePlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")), true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 0.8F));

            zombieMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getZombieMiniGame().getKaelaEvent(), new ZombieFireworkTask(this, winnerTeam), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getZombieMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(zombieMatchProperties.getFireworkTaskId());
                zombieMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getZombieMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (ZombiePlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);

                ZombieItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
            });

            // Reset properties
            winnerTeam = null;
            zombieMatchProperties.resetTimer();

            setState(ZombieMatchState.WAITING);
        }, 120L);
    }

    public void checkWinner() {
        winnerTeam = this.teamHelper
                .getTeamList()
                .stream()
                .filter(ZombieTeam.class::isInstance)
                .map(ZombieTeam.class::cast)
                .sorted(Comparator.comparingInt(ZombieTeam::getScore).reversed())
                .toList()
                .getFirst();

        stop();
    }

    public void spawnZombies() {
        if (getAvailableSpawnSize() <= 0) return;

        final List<EntitySpot> availableEntitySpots = new ArrayList<>(zombies.values().stream().filter(EntitySpot::isAvailable).toList());
        Collections.shuffle(availableEntitySpots);

        availableEntitySpots.forEach(EntitySpot::summonEntity);

        announcer.sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
    }

    public void clearZombies() {
        zombies.forEach((uuid, spot) -> spot.clearEntities());

        arenaSpawnLocation
                .getWorld()
                .getEntities()
                .stream()
                .filter(entity -> NBTMetadataUtil.hasEntityString(entity, "zombiespot_id"))
                .forEach(Entity::remove);
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(zombieMatchProperties.getStartingTaskId());
        zombieMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(zombieMatchProperties.getPreLobbyTaskId());
        zombieMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(zombieMatchProperties.getArenaTickTaskId());
        zombieMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(zombieMatchProperties.getArenaTimeTaskId());
        zombieMatchProperties.setArenaTimeTaskId(0);
    }

    public int getTimeLeft() {
        return timeLimit - gameTime;
    }

    public int getAvailableSpawnSize() {
        return zombies.values().stream().filter(EntitySpot::isAvailable).toList().size();
    }
}
