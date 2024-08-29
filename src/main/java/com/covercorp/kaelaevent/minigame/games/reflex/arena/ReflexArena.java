package com.covercorp.kaelaevent.minigame.games.reflex.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.reflex.ReflexMiniGame;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.bar.ReflexScoreBarHelper;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.event.ReflexRoundStartEvent;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.listener.ReflexMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.listener.ReflexMatchListener;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.properties.ReflexMatchProperties;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.ReflexSpot;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.state.ReflexMatchState;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.task.ReflexFireworkTask;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.task.ReflexPreLobbyTask;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.task.ReflexTickTask;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.task.ReflexTimeTask;
import com.covercorp.kaelaevent.minigame.games.reflex.inventory.ReflexItemCollection;
import com.covercorp.kaelaevent.minigame.games.reflex.player.ReflexPlayer;
import com.covercorp.kaelaevent.minigame.games.reflex.team.ReflexTeam;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
public final class ReflexArena {
    private final ReflexMiniGame reflexMiniGame;

    private final PlayerHelper<ReflexMiniGame> playerHelper;
    private final TeamHelper<ReflexMiniGame, ReflexPlayer> teamHelper;
    private final Announcer<ReflexMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final Location lobbyLocation;
    private final Location arenaCenterLocation;

    private final Map<UUID, ReflexSpot> spots;

    private final ReflexMatchProperties reflexMatchProperties;

    private final ReflexScoreBarHelper scoreBarHelper;

    @Setter(AccessLevel.PUBLIC) private int gameTime;
    @Setter(AccessLevel.PUBLIC) private ReflexMatchState state;
    @Setter(AccessLevel.PUBLIC) private ReflexTeam winnerTeam;

    public ReflexArena(ReflexMiniGame reflexMiniGame) {
        this.reflexMiniGame = reflexMiniGame;

        playerHelper = reflexMiniGame.getPlayerHelper();
        teamHelper = reflexMiniGame.getTeamHelper();
        announcer = reflexMiniGame.getAnnouncer();

        gameMiniMessage = reflexMiniGame.getMiniMessage();

        lobbyLocation = reflexMiniGame.getConfigHelper().getLobbySpawn();
        arenaCenterLocation = reflexMiniGame.getConfigHelper().getArenaCenter();

        spots = new ConcurrentHashMap<>();

        getReflexMiniGame().getConfigHelper().getSpotIds().forEach(spotId -> {
            final ReflexSpot spot = new ReflexSpot(this, spotId);

            spots.put(spot.getUniqueId(), spot);
        });

        reflexMatchProperties = new ReflexMatchProperties(this);

        scoreBarHelper = new ReflexScoreBarHelper(this);

        winnerTeam = null;

        clearUnregisteredEntities();

        Bukkit.getServer().getPluginManager().registerEvents(new ReflexMatchListener(this), getReflexMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new ReflexMatchGameListener(this), getReflexMiniGame().getKaelaEvent());

        setState(ReflexMatchState.WAITING);
    }

    public void start() {
        Bukkit.getScheduler().cancelTask(getReflexMatchProperties().getStartingTaskId());

        reflexMatchProperties.setStartingTaskId(0);

        clearUnregisteredEntities();

        // Spawn spots
        spots.forEach((uuid, spot) -> spot.spawnParts());

        final Iterator<ReflexSpot> spotIterator = spots.values().iterator();

        for (final MiniGamePlayer<ReflexMiniGame> participant : playerHelper.getPlayerList()) {
            if (!spotIterator.hasNext()) {
                announcer.sendGlobalMessage("&c&lMatch start cancelled due to insufficient Spots.", false);
                stop();
                return;
            }

            // Two players
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) return;

            final ReflexTeam team = (ReflexTeam) participant.getMiniGameTeam();
            if (team == null) return;

            team.setReflexSpot(spotIterator.next());

            final Location chairLoc = team.getReflexSpot().getSpotSpawn();
            if (chairLoc == null) {
                announcer.sendGlobalMessage("&c&lMatch start cancelled due to invalid spot chair location.", false);
                stop();
                return;
            }

            // Get a spot and sit them
            player.teleport(team.getReflexSpot().getSpotSpawn());
            team.getReflexSpot().sitPlayer((ReflexPlayer) participant);

            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        announcer.sendGlobalTitle(Title.title(gameMiniMessage
                        .deserialize("<gold><bold>REFLEX MATCH"), this.gameMiniMessage
                        .deserialize("<gray>Get ready!"),
                Title.Times.times(Duration.ofMillis(0L), Duration.ofMillis(2000L), Duration.ofMillis(1000L)))
        );
        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lReflex Match", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lWait until Kaela's screen changes to press", true);
        announcer.sendGlobalMessage("&e&lthe button in front of you.", true);
        announcer.sendGlobalMessage("&e&lThe first talent who presses de button wins the round.", true);
        announcer.sendGlobalMessage("&e&lBeware of pressing the button too early!", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        reflexMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(this.reflexMiniGame.getKaelaEvent(), new ReflexPreLobbyTask(this), 0L, 20L).getTaskId()
        );

        setState(ReflexMatchState.ARENA_STARTING);
    }

    public void postStart() {
        reflexMatchProperties.setPreLobby(false);

        Bukkit.getScheduler().cancelTask(this.reflexMatchProperties.getPreLobbyTaskId());

        reflexMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getReflexMiniGame().getKaelaEvent(), new ReflexTickTask(this), 0L, 1L).getTaskId()
        );
        reflexMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getReflexMiniGame().getKaelaEvent(), new ReflexTimeTask(this), 0L, 20L).getTaskId()
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

        setState(ReflexMatchState.GAME);

        Bukkit.getPluginManager().callEvent(new ReflexRoundStartEvent(this));
    }

    public void stop() {
        setState(ReflexMatchState.ENDING);

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

            reflexMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getReflexMiniGame().getKaelaEvent(), new ReflexFireworkTask(this, winnerTeam), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getReflexMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(reflexMatchProperties.getFireworkTaskId());

                reflexMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getReflexMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (ReflexPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                final ReflexTeam team = (ReflexTeam) gamePlayer.getMiniGameTeam();
                if (team == null) return;
                final ReflexSpot spot = team.getReflexSpot();
                if (spot == null) return;

                spot.unSitPlayer(gamePlayer);
                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.clearActivePotionEffects();
                player.setFlying(true);

                ReflexItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().stream().map(g->(ReflexTeam)g).forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
                team.setScore(0);
            });

            // Reset properties
            winnerTeam = null;
            reflexMatchProperties.resetTimer();
            resetSpots();

            setState(ReflexMatchState.WAITING);
        }, 120L);
    }

    public void resetSpots() {
        spots.values().forEach(ReflexSpot::deSpawnParts);
    }

    public void clearUnregisteredEntities() {
        arenaCenterLocation.getChunk().load();

        arenaCenterLocation.getWorld().getEntitiesByClass(ItemDisplay.class)
                .stream()
                .filter(entity -> NBTMetadataUtil.hasEntityString(entity, "reflex_button"))
                .forEach(Entity::remove);
        arenaCenterLocation.getWorld().getEntitiesByClass(ItemDisplay.class)
                .stream()
                .filter(entity -> NBTMetadataUtil.hasEntityString(entity, "reflex_chair"))
                .forEach(Entity::remove);
        arenaCenterLocation.getWorld().getEntitiesByClass(ItemDisplay.class)
                .stream()
                .filter(entity -> NBTMetadataUtil.hasEntityString(entity, "reflex_screen"))
                .forEach(Entity::remove);
        arenaCenterLocation.getWorld().getEntitiesByClass(Interaction.class)
                .stream().filter(entity -> NBTMetadataUtil.hasEntityString(entity, "reflex_button"))
                .forEach(Entity::remove);
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(reflexMatchProperties.getStartingTaskId());
        reflexMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(reflexMatchProperties.getPreLobbyTaskId());
        reflexMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(reflexMatchProperties.getArenaTickTaskId());
        reflexMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(reflexMatchProperties.getArenaTimeTaskId());
        reflexMatchProperties.setArenaTimeTaskId(0);
    }

    public ReflexTeam getOppositeTeam(final ReflexTeam team) {
        return getTeamHelper().getTeamList()
                .stream()
                .map(g -> (ReflexTeam) g)
                .filter(t -> !t.getIdentifier().equals(team.getIdentifier()))
                .findFirst()
                .orElse(null);
    }
}