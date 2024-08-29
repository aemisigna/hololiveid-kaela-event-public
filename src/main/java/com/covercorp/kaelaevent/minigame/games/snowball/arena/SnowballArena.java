package com.covercorp.kaelaevent.minigame.games.snowball.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.snowball.SnowballMiniGame;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.bar.SnowballTimeBarHelper;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.light.SnowballLightPair;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.light.light.SnowballLight;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.light.light.side.LightSide;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.listener.SnowballMatchListener;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.listener.SnowballMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.machine.IceMachine;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.machine.ScoreMachine;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.properties.SnowballMatchProperties;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.state.SnowballMatchState;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.task.SnowballFireworkTask;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.task.SnowballPreLobbyTask;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.task.SnowballTickTask;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.task.SnowballTimeTask;
import com.covercorp.kaelaevent.minigame.games.snowball.inventory.SnowballItemCollection;
import com.covercorp.kaelaevent.minigame.games.snowball.player.SnowballPlayer;
import com.covercorp.kaelaevent.minigame.games.snowball.team.SnowballTeam;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
public final class SnowballArena {
    private final SnowballMiniGame snowballMiniGame;

    private final PlayerHelper<SnowballMiniGame> playerHelper;
    private final TeamHelper<SnowballMiniGame, SnowballPlayer> teamHelper;
    private final Announcer<SnowballMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final Location lobbyLocation;
    private final List<Location> arenaSpawnLocations;
    private final List<Location> iceMachineLocations;
    private final List<Location> scoreMachineLocations;

    private final SnowballMatchProperties snowballMatchProperties;

    private final SnowballTimeBarHelper snowballTimeBarHelper;

    private final Map<UUID, SnowballLightPair> snowballLightPairs;

    @Setter(AccessLevel.PUBLIC) private int gameTime;

    @Setter(AccessLevel.PUBLIC) private SnowballMatchState state;

    @Setter(AccessLevel.PUBLIC) private SnowballTeam winnerTeam;

    public SnowballArena(SnowballMiniGame snowballMiniGame) {
        this.snowballMiniGame = snowballMiniGame;

        playerHelper = snowballMiniGame.getPlayerHelper();
        teamHelper = snowballMiniGame.getTeamHelper();
        announcer = snowballMiniGame.getAnnouncer();

        gameMiniMessage = snowballMiniGame.getMiniMessage();

        lobbyLocation = snowballMiniGame.getConfigHelper().getLobbySpawn();
        arenaSpawnLocations = snowballMiniGame.getConfigHelper().getArenaSpawns();
        iceMachineLocations = snowballMiniGame.getConfigHelper().getIceMachineSpawns();
        scoreMachineLocations = snowballMiniGame.getConfigHelper().getScoreMachineSpawns();

        snowballMatchProperties = new SnowballMatchProperties(this);

        snowballTimeBarHelper = new SnowballTimeBarHelper(this);

        snowballLightPairs = new ConcurrentHashMap<>();

        winnerTeam = null;

        clearOldLights();

        Bukkit.getServer().getPluginManager().registerEvents(new SnowballMatchListener(this), getSnowballMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new SnowballMatchGameListener(this), getSnowballMiniGame().getKaelaEvent());

        // Setup team machines
        int index = 0;
        for (final SnowballTeam team : teamHelper.getTeamList().stream().map(g -> (SnowballTeam)g).toList()) {
            final Location spawn = arenaSpawnLocations.get(index);
            team.setSpawnPoint(spawn);

            final Location iceMachineLocation = iceMachineLocations.get(index);
            final IceMachine iceMachine = new IceMachine(this, iceMachineLocation);
            team.setIceMachine(iceMachine);

            final Location scoreMachineLocation = scoreMachineLocations.get(index);
            final ScoreMachine scoreMachine = new ScoreMachine(this, scoreMachineLocation);
            team.setScoreMachine(scoreMachine);

            index++;
        }

        // Setup targets
        getSnowballMiniGame().getConfigHelper().getLights().forEach(lightId -> {
            final Pair<Location, Location> locationPair = getSnowballMiniGame().getConfigHelper().getLightPairs(lightId);
            final SnowballLightPair pair = new SnowballLightPair(UUID.randomUUID(), this, locationPair.key(), locationPair.value());

            snowballLightPairs.put(pair.getUniqueId(), pair);
        });

        setState(SnowballMatchState.WAITING);
    }

    public void start() {
        Bukkit.getScheduler().cancelTask(getSnowballMatchProperties().getStartingTaskId());

        snowballMatchProperties.setStartingTaskId(0);

        clearOldLights();

        spawnIceMachines();
        spawnScoreMachines();

        for (final MiniGamePlayer<SnowballMiniGame> participant : playerHelper.getPlayerList()) {
            // Two players
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) return;

            final SnowballTeam team = (SnowballTeam) participant.getMiniGameTeam();
            if (team == null) return;

            player.teleport(team.getSpawnPoint());
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        announcer.sendGlobalTitle(Title.title(this.gameMiniMessage
                        .deserialize("<gold><bold>SNOWBALL RACE"), this.gameMiniMessage
                        .deserialize("<gray>Get ready!"),
                Title.Times.times(
                        Duration.ofMillis(0L),
                        Duration.ofMillis(2000L),
                        Duration.ofMillis(1000L))));
        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lSnowball Race", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lSteal your opponents lights by shooting the targets.", true);
        announcer.sendGlobalMessage("&e&lEach target can only have one light for a single team.", true);
        announcer.sendGlobalMessage("&e&lEvery 15 seconds a TNT will explode to the talent with", true);
        announcer.sendGlobalMessage("&e&lless lights, dealing fatal damage.", true);
        announcer.sendGlobalMessage("&e&lThe talent who survives, wins the match.", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        snowballMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(this.snowballMiniGame.getKaelaEvent(), new SnowballPreLobbyTask(this), 0L, 20L).getTaskId()
        );

        setState(SnowballMatchState.ARENA_STARTING);
    }

    public void postStart() {
        snowballMatchProperties.setPreLobby(false);

        Bukkit.getScheduler().cancelTask(this.snowballMatchProperties.getPreLobbyTaskId());

        snowballMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getSnowballMiniGame().getKaelaEvent(), new SnowballTickTask(this), 0L, 1L).getTaskId()
        );
        snowballMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getSnowballMiniGame().getKaelaEvent(), new SnowballTimeTask(this), 0L, 20L).getTaskId()
        );

        snowballTimeBarHelper.start();

        announcer.sendGlobalMessage("&eStart lightning up!", false);
        announcer.sendGlobalTitle(Title.title(this.gameMiniMessage
                        .deserialize("<gold><bold>Start!"),
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(0L),
                        Duration.ofMillis(1000L),
                        Duration.ofMillis(500L))));
        announcer.sendGlobalSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0F, 2.0F);

        resetLights();

        setState(SnowballMatchState.GAME);
    }

    public void stop() {
        setState(SnowballMatchState.ENDING);

        deSpawnIceMachines();
        deSpawnScoreMachines();
        removeLights();
        stopTasks();

        snowballTimeBarHelper.stop();

        if (winnerTeam == null) {
            announcer.sendGlobalMessage(" \n&6&lThe game ended without a winner...", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);
        } else {
            announcer.sendGlobalTitle(
                    Title.title(
                            gameMiniMessage.deserialize("<green>Game ended!"),
                            gameMiniMessage.deserialize("<aqua>" + winnerTeam.getFirstPlayer().getName() + " <white>survived the game!"),
                            Title.Times.times(Duration.ofMillis(0L), Duration.ofMillis(5000L), Duration.ofMillis(1000L)))
            );

            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
            announcer.sendGlobalMessage("&e&lMatch ended!", true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&eWinner team: &f" + LegacyComponentSerializer.legacyAmpersand().serialize(winnerTeam.getBetterPrefix()), true);
            announcer.sendGlobalMessage(winnerTeam.getPlayers().stream().map(MiniGamePlayer::getName).collect(Collectors.joining("&f & ")), true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 0.8F));

            snowballMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getSnowballMiniGame().getKaelaEvent(), new SnowballFireworkTask(this, winnerTeam), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getSnowballMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(snowballMatchProperties.getFireworkTaskId());

                snowballMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getSnowballMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (SnowballPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.clearActivePotionEffects();
                player.setFlying(true);

                SnowballItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().stream().map(g->(SnowballTeam)g).forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
                team.setScore(3);
            });

            // Reset properties
            winnerTeam = null;
            snowballMatchProperties.resetTimer();

            setState(SnowballMatchState.WAITING);
        }, 120L);
    }
    
    public void removeLights() {
        snowballLightPairs.values().forEach(pair -> pair.getLights().forEach(SnowballLight::deSpawn));
    }

    public void resetLights() {
        int index = 0;
        for (final SnowballLightPair pair : snowballLightPairs.values()) {
            pair.getLights().forEach(SnowballLight::spawn);

            if (index <= 2) {
                pair.lightUpLight(LightSide.LEFT);
            }
            if (index > 2 && index <= 5) {
                pair.lightUpLight(LightSide.RIGHT);
            }
            index++;
        }
        
        announcer.sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
        announcer.sendGlobalMessage("&7[!] Lights have been reset.", false);
    }

    public void clearOldLights() {
        lobbyLocation.getWorld().getEntitiesByClass(ItemDisplay.class)
                .stream().filter(entity -> NBTMetadataUtil.hasEntityString(entity, "snowball_race"))
                .forEach(Entity::remove);
        lobbyLocation.getWorld().getEntitiesByClass(Interaction.class)
                .stream().filter(entity -> NBTMetadataUtil.hasEntityString(entity, "snowball_race"))
                .forEach(Entity::remove);
    }

    public void spawnIceMachines() {
        teamHelper.getTeamList().stream().map(g -> (SnowballTeam)g).forEach(team -> {
            if (team.getIceMachine() == null) return;
            team.getIceMachine().spawn();
        });
    }

    public void deSpawnIceMachines() {
        teamHelper.getTeamList().stream().map(g -> (SnowballTeam)g).forEach(team -> {
            if (team.getIceMachine() == null) return;
            team.getIceMachine().deSpawn();
        });
    }

    public void spawnScoreMachines() {
        teamHelper.getTeamList().stream().map(g -> (SnowballTeam)g).forEach(team -> {
            if (team.getScoreMachine() == null) return;
            team.getScoreMachine().spawn();
        });
    }

    public void deSpawnScoreMachines() {
        teamHelper.getTeamList().stream().map(g -> (SnowballTeam)g).forEach(team -> {
            if (team.getScoreMachine() == null) return;
            team.getScoreMachine().deSpawn();
        });
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(snowballMatchProperties.getStartingTaskId());
        snowballMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(snowballMatchProperties.getPreLobbyTaskId());
        snowballMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(snowballMatchProperties.getArenaTickTaskId());
        snowballMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(snowballMatchProperties.getArenaTimeTaskId());
        snowballMatchProperties.setArenaTimeTaskId(0);
    }

    public SnowballTeam getOppositeTeam(final SnowballTeam team) {
        return getTeamHelper().getTeamList()
                .stream()
                .map(g->(SnowballTeam)g)
                .filter(t -> !t.getIdentifier().equals(team.getIdentifier()))
                .findFirst()
                .orElse(null);
    }

    public SnowballTeam getExplodableTeam() {
        final List<SnowballTeam> teams = getTeamHelper().getTeamList()
                .stream()
                .map(g -> (SnowballTeam) g)
                .toList();

        if (teams.size() != 2) {
            return null;
        }

        SnowballTeam team1 = teams.get(0);
        SnowballTeam team2 = teams.get(1);

        int score1 = team1.getScore();
        int score2 = team2.getScore();

        if (score1 < score2) {
            return team1;
        } else if (score2 < score1) {
            return team2;
        } else {
            return null;
        }
    }
}