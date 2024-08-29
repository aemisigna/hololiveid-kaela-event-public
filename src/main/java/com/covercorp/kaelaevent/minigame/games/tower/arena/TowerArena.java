package com.covercorp.kaelaevent.minigame.games.tower.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.tower.TowerMiniGame;
import com.covercorp.kaelaevent.minigame.games.tower.arena.bar.TowerScoreBarHelper;
import com.covercorp.kaelaevent.minigame.games.tower.arena.event.TowerRoundStartEvent;
import com.covercorp.kaelaevent.minigame.games.tower.arena.listener.TowerMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.tower.arena.listener.TowerMatchListener;
import com.covercorp.kaelaevent.minigame.games.tower.arena.properties.TowerMatchProperties;
import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.TowerSpot;
import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.part.status.GambleMachineStatus;
import com.covercorp.kaelaevent.minigame.games.tower.arena.state.TowerMatchState;
import com.covercorp.kaelaevent.minigame.games.tower.arena.task.TowerFireworkTask;
import com.covercorp.kaelaevent.minigame.games.tower.arena.task.TowerPreLobbyTask;
import com.covercorp.kaelaevent.minigame.games.tower.arena.task.TowerTickTask;
import com.covercorp.kaelaevent.minigame.games.tower.arena.task.TowerTimeTask;
import com.covercorp.kaelaevent.minigame.games.tower.inventory.TowerItemCollection;
import com.covercorp.kaelaevent.minigame.games.tower.player.TowerPlayer;
import com.covercorp.kaelaevent.minigame.games.tower.team.TowerTeam;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
public final class TowerArena {
    private final TowerMiniGame towerMiniGame;

    private final PlayerHelper<TowerMiniGame> playerHelper;
    private final TeamHelper<TowerMiniGame, TowerPlayer> teamHelper;
    private final Announcer<TowerMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final Location lobbyLocation;
    private final Location arenaCenterLocation;

    private final Map<UUID, TowerSpot> spots;

    private final TowerMatchProperties towerMatchProperties;

    private final TowerScoreBarHelper scoreBarHelper;

    @Setter(AccessLevel.PUBLIC) private int gameTime;
    @Setter(AccessLevel.PUBLIC) private TowerMatchState state;
    @Setter(AccessLevel.PUBLIC) private TowerTeam winnerTeam;

    public TowerArena(TowerMiniGame towerMiniGame) {
        this.towerMiniGame = towerMiniGame;

        playerHelper = towerMiniGame.getPlayerHelper();
        teamHelper = towerMiniGame.getTeamHelper();
        announcer = towerMiniGame.getAnnouncer();

        gameMiniMessage = towerMiniGame.getMiniMessage();

        lobbyLocation = towerMiniGame.getConfigHelper().getLobbySpawn();
        arenaCenterLocation = towerMiniGame.getConfigHelper().getArenaCenter();

        spots = new ConcurrentHashMap<>();

        getTowerMiniGame().getConfigHelper().getSpotIds().forEach(spotId -> {
            final TowerSpot spot = new TowerSpot(this, spotId);

            spots.put(spot.getUniqueId(), spot);
        });

        towerMatchProperties = new TowerMatchProperties(this);

        scoreBarHelper = new TowerScoreBarHelper(this);

        winnerTeam = null;

        clearUnregisteredEntities();

        Bukkit.getServer().getPluginManager().registerEvents(new TowerMatchListener(this), getTowerMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new TowerMatchGameListener(this), getTowerMiniGame().getKaelaEvent());

        setState(TowerMatchState.WAITING);
    }

    public void start() {
        Bukkit.getScheduler().cancelTask(getTowerMatchProperties().getStartingTaskId());

        towerMatchProperties.setStartingTaskId(0);

        clearUnregisteredEntities();

        // Spawn spots
        spots.forEach((uuid, spot) -> spot.spawnParts());

        final Iterator<TowerSpot> spotIterator = spots.values().iterator();

        for (final MiniGamePlayer<TowerMiniGame> participant : playerHelper.getPlayerList()) {
            if (!spotIterator.hasNext()) {
                announcer.sendGlobalMessage("&c&lMatch start cancelled due to insufficient Spots.", false);
                stop();
                return;
            }

            // Two players
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) return;

            final TowerTeam team = (TowerTeam) participant.getMiniGameTeam();
            if (team == null) return;

            team.setTowerSpot(spotIterator.next());

            // Get a spot and sit them
            player.teleport(team.getTowerSpot().getSpawnLocation());
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>TOWER OF LUCK"),
                gameMiniMessage.deserialize("<gray>Get ready!"),
                Title.Times.times(Duration.ofMillis(0L), Duration.ofMillis(2000L), Duration.ofMillis(1000L)))
        );
        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lThe Tower of Luck", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lGamble your machine to get a prize.", true);
        announcer.sendGlobalMessage("&e&lThe talent with the most valuable prize wins", true);
        announcer.sendGlobalMessage("&e&lthe round.", true);
        announcer.sendGlobalMessage("&e&lThe first Talent who gets 5 score wins the match.", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        towerMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(this.towerMiniGame.getKaelaEvent(), new TowerPreLobbyTask(this), 0L, 20L).getTaskId()
        );

        setState(TowerMatchState.ARENA_STARTING);
    }

    public void postStart() {
        towerMatchProperties.setPreLobby(false);

        Bukkit.getScheduler().cancelTask(towerMatchProperties.getPreLobbyTaskId());

        towerMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getTowerMiniGame().getKaelaEvent(), new TowerTickTask(this), 0L, 1L).getTaskId()
        );
        towerMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getTowerMiniGame().getKaelaEvent(), new TowerTimeTask(this), 0L, 20L).getTaskId()
        );

        scoreBarHelper.start();

        announcer.sendGlobalMessage("&eGame started!", false);
        announcer.sendGlobalTitle(Title.title(this.gameMiniMessage
                        .deserialize("<gold><bold>Start!"),
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(0L),
                        Duration.ofMillis(1000L),
                        Duration.ofMillis(500L))));
        announcer.sendGlobalSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0F, 2.0F);

        setState(TowerMatchState.GAME);

        Bukkit.getPluginManager().callEvent(new TowerRoundStartEvent(this));
    }

    public void stop() {
        setState(TowerMatchState.ENDING);

        stopTasks();

        scoreBarHelper.stop();

        if (winnerTeam == null) {
            announcer.sendGlobalMessage(" \n&6&lThe game ended without a winner...", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);
        } else {
            announcer.sendGlobalTitle(
                    Title.title(
                            gameMiniMessage.deserialize("<green>Game ended!"),
                            gameMiniMessage.deserialize("<aqua>" + winnerTeam.getFirstPlayer().getName() + " <white>won the match!"),
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

            towerMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getTowerMiniGame().getKaelaEvent(), new TowerFireworkTask(this, winnerTeam), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getTowerMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(towerMatchProperties.getFireworkTaskId());

                towerMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getTowerMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (TowerPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                final TowerTeam team = (TowerTeam) gamePlayer.getMiniGameTeam();
                if (team == null) return;
                final TowerSpot spot = team.getTowerSpot();
                if (spot == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.clearActivePotionEffects();
                player.setFlying(true);

                TowerItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().stream().map(g->(TowerTeam)g).forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
                team.setScore(0);
            });

            // Reset properties
            winnerTeam = null;
            towerMatchProperties.resetTimer();
            resetSpots();

            setState(TowerMatchState.WAITING);
        }, 120L);
    }

    public void resetSpots() {
        spots.values().forEach(TowerSpot::deSpawnParts);
    }

    public void clearUnregisteredEntities() {
        arenaCenterLocation.getChunk().load();

        arenaCenterLocation.getWorld().getEntitiesByClass(ItemDisplay.class)
                .stream()
                .filter(entity -> NBTMetadataUtil.hasEntityString(entity, "gamble_machine"))
                .forEach(Entity::remove);
        arenaCenterLocation.getWorld().getEntitiesByClass(Interaction.class)
                .stream().filter(entity -> NBTMetadataUtil.hasEntityString(entity, "gamble_machine"))
                .forEach(Entity::remove);
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(towerMatchProperties.getStartingTaskId());
        towerMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(towerMatchProperties.getPreLobbyTaskId());
        towerMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(towerMatchProperties.getArenaTickTaskId());
        towerMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(towerMatchProperties.getArenaTimeTaskId());
        towerMatchProperties.setArenaTimeTaskId(0);
    }

    public boolean allMachinesRolled() {
        return getTeamHelper().getTeamList()
                .stream()
                .map(g -> (TowerTeam) g)
                .filter(team -> !team.getTowerSpot().getGambleMachine().isRolled())
                .toList()
                .isEmpty();
    }

    public TowerTeam getRoundWinner() {
        final List<TowerTeam> towerTeams = getTeamHelper().getTeamList().stream()
                .map(g -> (TowerTeam) g)
                .toList();

        TowerTeam roundWinner = null;
        int highestLevel = Integer.MIN_VALUE;

        for (final TowerTeam towerTeam : towerTeams) {
            final GambleMachineStatus gambleMachineStatus = towerTeam.getTowerSpot().getGambleMachine().getStatus();
            int currentLevel = gambleMachineStatus.getLevel();
            if (currentLevel > highestLevel) {
                highestLevel = currentLevel;
                roundWinner = towerTeam;
            } else if (currentLevel == highestLevel) {
                roundWinner = null;
            }
        }

        return roundWinner;
    }

    public TowerTeam getOppositeTeam(final TowerTeam team) {
        return getTeamHelper().getTeamList()
                .stream()
                .map(g -> (TowerTeam) g)
                .filter(t -> !t.getIdentifier().equals(team.getIdentifier()))
                .findFirst()
                .orElse(null);
    }
}