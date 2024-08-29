package com.covercorp.kaelaevent.minigame.games.target.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.target.TargetMiniGame;
import com.covercorp.kaelaevent.minigame.games.target.arena.bar.TargetTimeBarHelper;
import com.covercorp.kaelaevent.minigame.games.target.arena.listener.TargetMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.target.arena.listener.TargetMatchListener;
import com.covercorp.kaelaevent.minigame.games.target.arena.properties.TargetMatchProperties;
import com.covercorp.kaelaevent.minigame.games.target.arena.scoreboard.TargetScoreboardHelper;
import com.covercorp.kaelaevent.minigame.games.target.arena.state.TargetMatchState;
import com.covercorp.kaelaevent.minigame.games.target.arena.target.ArrowGenerator;
import com.covercorp.kaelaevent.minigame.games.target.arena.target.ShootableTarget;
import com.covercorp.kaelaevent.minigame.games.target.arena.task.*;
import com.covercorp.kaelaevent.minigame.games.target.inventory.TargetItemCollection;
import com.covercorp.kaelaevent.minigame.games.target.player.TargetPlayer;
import com.covercorp.kaelaevent.minigame.games.target.team.TargetTeam;
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
public final class TargetArena {
    private final TargetMiniGame targetMiniGame;

    private final PlayerHelper<TargetMiniGame> playerHelper;
    private final TeamHelper<TargetMiniGame, TargetPlayer> teamHelper;
    private final Announcer<TargetMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final TargetScoreboardHelper targetScoreboardHelper;

    private final Location lobbyLocation;
    private final Location arenaSpawnLocation;
    private final Location arenaCenter;

    private final TargetMatchProperties targetMatchProperties;

    private final TargetTimeBarHelper timeBarHelper;

    private final int timeLimit;
    private final static int DEFAULT_TIME_LIMIT = 180; //60 * 2;

    @Setter(AccessLevel.PUBLIC) private int gameTime;
    @Setter(AccessLevel.PUBLIC) private TargetMatchState state;

    private final Map<UUID, ShootableTarget> targets;
    private final ArrowGenerator arrowGenerator;
    private List<TargetTeam> winnerResults;

    public TargetArena(final TargetMiniGame targetMiniGame) {
        this.targetMiniGame = targetMiniGame;

        playerHelper = targetMiniGame.getPlayerHelper();
        teamHelper = targetMiniGame.getTeamHelper();
        announcer = targetMiniGame.getAnnouncer();

        gameMiniMessage = targetMiniGame.getMiniMessage();

        targetScoreboardHelper = new TargetScoreboardHelper(this);
        lobbyLocation = targetMiniGame.getConfigHelper().getLobbySpawn();
        arenaSpawnLocation = targetMiniGame.getConfigHelper().getArenaSpawn();
        arenaCenter = targetMiniGame.getConfigHelper().getArenaCenter();

        timeBarHelper = new TargetTimeBarHelper(this);

        targetMatchProperties = new TargetMatchProperties(this);

        timeLimit = DEFAULT_TIME_LIMIT; // 2 mins

        targets = new ConcurrentHashMap<>();
        arrowGenerator = new ArrowGenerator(UUID.randomUUID(), this.arenaCenter, this.arenaCenter.clone().add(0.0, -2.0, 0.0));
        targetMiniGame.getConfigHelper().getTargetLocations().forEach(targetLocation -> {
            final UUID uuid = UUID.randomUUID();
            final ShootableTarget target = new ShootableTarget(uuid, this.arenaCenter, targetLocation);

            this.targets.put(uuid, target);
        });
        
        winnerResults = List.of();
        clearTargets();
        
        Bukkit.getServer().getPluginManager().registerEvents(new TargetMatchListener(this), getTargetMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new TargetMatchGameListener(this), getTargetMiniGame().getKaelaEvent());

        setState(TargetMatchState.WAITING);
    }

    public void start() {
        // Cancel the match start item task
        Bukkit.getScheduler().cancelTask(getTargetMatchProperties().getStartingTaskId());
        targetMatchProperties.setStartingTaskId(0);

        clearTargets();
        arrowGenerator.spawn();

        setGameTime(0);

        this.playerHelper.getPlayerList().forEach(participant -> {
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) {
                return;
            }
            final TargetTeam team = (TargetTeam)participant.getMiniGameTeam();
            if (team == null) {
                return;
            }
            player.teleport(this.getArenaSpawnLocation());
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setFlying(false);
        });

        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>TARGET SHOOTING"),
                gameMiniMessage.deserialize("<gray>Get ready!"),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(1000)
                )
        ));

        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);

        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lTarget Shooting Range", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lPay attention to the targets that will start", true);
        announcer.sendGlobalMessage("&e&lappearing in front of you.", true);
        announcer.sendGlobalMessage("&e&lShoot them to earn score and win the match!", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        // The task constructor has the setPreLobby(true) func
        targetMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(targetMiniGame.getKaelaEvent(), new TargetPreLobbyTask(this), 0L, 20L).getTaskId()
        );
        setState(TargetMatchState.ARENA_STARTING);
    }

    public void postStart() {
        // Cancel the previous task
        targetMatchProperties.setPreLobby(false);
        Bukkit.getScheduler().cancelTask(targetMatchProperties.getPreLobbyTaskId());

        targetMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getTargetMiniGame().getKaelaEvent(), new TargetTickTask(this), 0L, 1L).getTaskId()
        );
        targetMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getTargetMiniGame().getKaelaEvent(), new TargetTimeTask(this), 0L, 20L).getTaskId()
        );

        timeBarHelper.start();

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

        spawnTargets(targets.size());

        setState(TargetMatchState.GAME);
    }

    public void stop() {
        setState(TargetMatchState.ENDING);

        stopTasks();
        timeBarHelper.stop();
        arrowGenerator.deSpawn();
        clearTargets();

        if (this.winnerResults.isEmpty()) {
            announcer.sendGlobalMessage(" \n&6&lThe game ended without a winner... let's try again!", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);
        } else {
            announcer.sendGlobalTitle(Title.title(gameMiniMessage.deserialize(
                    "<green>Game ended!"),
                    winnerResults.getFirst().getBetterPrefix()
                            .append(gameMiniMessage.deserialize("<gray>won the match!")),
                    Title.Times.times(Duration.ofMillis(0L), Duration.ofMillis(5000L), Duration.ofMillis(1000L)))
            );

            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
            announcer.sendGlobalMessage("&e&lMatch ended!", true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&eWinner team: &f" + LegacyComponentSerializer.legacyAmpersand().serialize(getWinnerResults().getFirst().getBetterPrefix()), true);
            announcer.sendGlobalMessage(getWinnerResults().getFirst().getPlayers().stream().map(TargetPlayer::getName).collect(Collectors.joining(" & ")), true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 0.8F));

            targetMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getTargetMiniGame().getKaelaEvent(), new TargetFireworkTask(this, winnerResults.getFirst()), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getTargetMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(targetMatchProperties.getFireworkTaskId());
                targetMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getTargetMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (TargetPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);

                TargetItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
            });

            // Reset properties
            winnerResults = List.of();
            targetMatchProperties.resetTimer();

            setState(TargetMatchState.WAITING);
        }, 120L);
    }

    public void setWinner() {
        this.winnerResults = this.teamHelper
                .getTeamList()
                .stream()
                .filter(TargetTeam.class::isInstance)
                .map(TargetTeam.class::cast)
                .sorted(Comparator.comparingInt(TargetTeam::getScore).reversed())
                .toList();
        this.stop();
    }

    public void spawnTargets(int count) {
        count -= getUsedTargetSize();
        if (count <= 0) return;

        List<ShootableTarget> availableTargets = new ArrayList<>(targets.values().stream().filter(target -> (target.getHitbox() == null || target.getHitbox().isDead())).toList());
        Collections.shuffle(availableTargets);
        List<ShootableTarget> targetRand = availableTargets.stream().limit(count).toList();
        targetRand.forEach(ShootableTarget::spawn);
        this.announcer.sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
        this.announcer.sendGlobalMessage("&a[Game] &eMore targets are spawning in the shooting range!", false);
    }

    public void clearTargets() {
        this.arenaCenter
                .getWorld()
                .getNearbyEntitiesByType(ItemDisplay.class, this.arenaCenter, 300.0)
                .stream()
                .filter(entity -> NBTMetadataUtil.hasEntityString(entity, "target_id"))
                .forEach(Entity::remove);
        this.arenaCenter
                .getWorld()
                .getNearbyEntitiesByType(Interaction.class, this.arenaCenter, 200.0)
                .stream()
                .filter(entity -> NBTMetadataUtil.hasEntityString(entity, "target_id"))
                .forEach(Entity::remove);
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(targetMatchProperties.getStartingTaskId());
        targetMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(targetMatchProperties.getPreLobbyTaskId());
        targetMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(targetMatchProperties.getArenaTickTaskId());
        targetMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(targetMatchProperties.getArenaTimeTaskId());
        targetMatchProperties.setArenaTimeTaskId(0);
    }

    public int getTimeLeft() {
        return timeLimit - gameTime;
    }

    public int getUsedTargetSize() {
        return this.targets.values().stream().filter(target -> target.getHitbox() != null).filter(target -> !target.getHitbox().isDead()).toList().size();
    }
}
