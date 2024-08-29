package com.covercorp.kaelaevent.minigame.games.trident.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.trident.TridentMiniGame;
import com.covercorp.kaelaevent.minigame.games.trident.arena.listener.TridentMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.trident.arena.listener.TridentMatchListener;
import com.covercorp.kaelaevent.minigame.games.trident.arena.properties.TridentMatchProperties;
import com.covercorp.kaelaevent.minigame.games.trident.arena.scoreboard.TridentScoreboardHelper;
import com.covercorp.kaelaevent.minigame.games.trident.arena.state.TridentMatchState;
import com.covercorp.kaelaevent.minigame.games.trident.arena.target.TridentTarget;
import com.covercorp.kaelaevent.minigame.games.trident.arena.task.TridentFireworkTask;
import com.covercorp.kaelaevent.minigame.games.trident.arena.task.TridentPreLobbyTask;
import com.covercorp.kaelaevent.minigame.games.trident.arena.task.TridentTickTask;
import com.covercorp.kaelaevent.minigame.games.trident.inventory.TridentItemCollection;
import com.covercorp.kaelaevent.minigame.games.trident.player.TridentPlayer;
import com.covercorp.kaelaevent.minigame.games.trident.team.TridentTeam;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
public final class TridentArena {
    private final TridentMiniGame tridentMiniGame;

    private final PlayerHelper<TridentMiniGame> playerHelper;
    private final TeamHelper<TridentMiniGame, TridentPlayer> teamHelper;
    private final Announcer<TridentMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final TridentScoreboardHelper tridentScoreboardHelper;

    private final Location lobbyLocation;
    private final Location arenaSpawnLocation;
    private final Location arenaCenter;

    private final TridentMatchProperties tridentMatchProperties;

    @Setter(AccessLevel.PUBLIC) private TridentMatchState state;

    private final Map<UUID, TridentTarget> targets;
    private List<TridentTeam> winnerResults;

    public TridentArena(final TridentMiniGame tridentMiniGame) {
        this.tridentMiniGame = tridentMiniGame;

        playerHelper = tridentMiniGame.getPlayerHelper();
        teamHelper = tridentMiniGame.getTeamHelper();
        announcer = tridentMiniGame.getAnnouncer();

        gameMiniMessage = tridentMiniGame.getMiniMessage();

        tridentScoreboardHelper = new TridentScoreboardHelper(this);
        lobbyLocation = tridentMiniGame.getConfigHelper().getLobbySpawn();
        arenaSpawnLocation = tridentMiniGame.getConfigHelper().getArenaSpawn();
        arenaCenter = tridentMiniGame.getConfigHelper().getArenaCenter();

        tridentMatchProperties = new TridentMatchProperties(this);

        targets = new ConcurrentHashMap<>();
        tridentMiniGame.getConfigHelper().getTridentTargetLocations().forEach(tridentLocation -> {
            final UUID uuid = UUID.randomUUID();
            final TridentTarget trident = new TridentTarget(uuid, arenaCenter, tridentLocation);

            targets.put(uuid, trident);
        });
        
        winnerResults = List.of();
        clearTridents();
        
        Bukkit.getServer().getPluginManager().registerEvents(new TridentMatchListener(this), getTridentMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new TridentMatchGameListener(this), getTridentMiniGame().getKaelaEvent());

        setState(TridentMatchState.WAITING);
    }

    public void start() {
        // Cancel the match start item task
        Bukkit.getScheduler().cancelTask(getTridentMatchProperties().getStartingTaskId());
        tridentMatchProperties.setStartingTaskId(0);

        clearTridents();

        this.playerHelper.getPlayerList().forEach(participant -> {
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) {
                return;
            }
            final TridentTeam team = (TridentTeam)participant.getMiniGameTeam();
            if (team == null) {
                return;
            }
            player.teleport(this.getArenaSpawnLocation());
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setFlying(false);
        });

        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>TRIDENT RACE"),
                gameMiniMessage.deserialize("<gray>Get ready!"),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(1000)
                )
        ));

        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);

        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lTrident Race", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lShoot the target that will appear in front of", true);
        announcer.sendGlobalMessage("&e&lyou before your opponent does", true);
        announcer.sendGlobalMessage("&e&lThe first talent to reach 20 points wins!", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        // The task constructor has the setPreLobby(true) func
        tridentMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(tridentMiniGame.getKaelaEvent(), new TridentPreLobbyTask(this), 0L, 20L).getTaskId()
        );
        setState(TridentMatchState.ARENA_STARTING);
    }

    public void postStart() {
        // Cancel the previous task
        tridentMatchProperties.setPreLobby(false);
        Bukkit.getScheduler().cancelTask(tridentMatchProperties.getPreLobbyTaskId());

        tridentMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getTridentMiniGame().getKaelaEvent(), new TridentTickTask(this), 0L, 1L).getTaskId()
        );

        announcer.sendGlobalMessage("&eShoot your tridents!", false);
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

        spawnTarget();

        setState(TridentMatchState.GAME);
    }

    public void stop() {
        setState(TridentMatchState.ENDING);

        stopTasks();
        clearTridents();

        if (this.winnerResults.isEmpty()) {
            announcer.sendGlobalMessage(" \n&6&lThe game ended without a winner... let's try again!", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);
        } else {
            announcer.sendGlobalTitle(Title.title(
                    gameMiniMessage.deserialize("<green>Game ended!"),
                    gameMiniMessage.deserialize(winnerResults.getFirst().getFirstPlayer().getName()),
                    Title.Times.times(Duration.ofMillis(0L), Duration.ofMillis(5000L), Duration.ofMillis(1000L)))
            );

            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
            announcer.sendGlobalMessage("&e&lMatch ended!", true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&eWinner team: &f" + LegacyComponentSerializer.legacyAmpersand().serialize(getWinnerResults().getFirst().getBetterPrefix()), true);
            announcer.sendGlobalMessage(getWinnerResults().getFirst().getPlayers().stream().map(TridentPlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")), true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 0.8F));

            tridentMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getTridentMiniGame().getKaelaEvent(), new TridentFireworkTask(this, winnerResults.getFirst()), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getTridentMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(tridentMatchProperties.getFireworkTaskId());
                tridentMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getTridentMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (TridentPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);

                TridentItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
            });

            // Reset properties
            winnerResults = List.of();
            tridentMatchProperties.resetTimer();

            setState(TridentMatchState.WAITING);
        }, 120L);
    }

    public void checkWinner() {
        winnerResults = teamHelper
                .getTeamList()
                .stream()
                .filter(TridentTeam.class::isInstance)
                .map(TridentTeam.class::cast)
                .sorted(Comparator.comparingInt(TridentTeam::getScore).reversed())
                .toList();

        stop();
    }

    public void spawnTarget() {
        if (getSpawnedTargets() >= 1) return;

        final List<TridentTarget> availableTridents = new ArrayList<>(targets.values().stream().filter(target -> (target.getHitbox() == null || target.getHitbox().isDead())).toList());
        Collections.shuffle(availableTridents);
        if (availableTridents.isEmpty()) return;

        final TridentTarget tridentTarget = availableTridents.getFirst();
        tridentTarget.spawn();

        tridentTarget.getLocation().getWorld().playSound(tridentTarget.getDisplay(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0F, 1.2F);

        //announcer.sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
    }

    public void clearTridents() {
        arenaSpawnLocation
                .getWorld()
                .getEntitiesByClass(ItemDisplay.class)
                .stream()
                .filter(entity -> NBTMetadataUtil.hasEntityString(entity, "target_id"))
                .forEach(Entity::remove);
        arenaSpawnLocation
                .getWorld()
                .getEntitiesByClass(Interaction.class)
                .stream()
                .filter(entity -> NBTMetadataUtil.hasEntityString(entity, "target_id"))
                .forEach(Entity::remove);
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(tridentMatchProperties.getStartingTaskId());
        tridentMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(tridentMatchProperties.getPreLobbyTaskId());
        tridentMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(tridentMatchProperties.getArenaTickTaskId());
        tridentMatchProperties.setArenaTickTaskId(0);
    }

    public int getSpawnedTargets() {
        return targets.values().stream().filter(target -> target.getHitbox() != null).filter(target -> !target.getHitbox().isDead()).toList().size();
    }
}
