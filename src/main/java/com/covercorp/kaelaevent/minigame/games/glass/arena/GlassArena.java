package com.covercorp.kaelaevent.minigame.games.glass.arena;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.glass.GlassMiniGame;
import com.covercorp.kaelaevent.minigame.games.glass.arena.bar.GlassTimeBarHelper;
import com.covercorp.kaelaevent.minigame.games.glass.arena.glass.BridgePart;
import com.covercorp.kaelaevent.minigame.games.glass.arena.listener.GlassMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.glass.arena.listener.GlassMatchListener;
import com.covercorp.kaelaevent.minigame.games.glass.arena.properties.GlassMatchProperties;
import com.covercorp.kaelaevent.minigame.games.glass.arena.state.GlassMatchState;
import com.covercorp.kaelaevent.minigame.games.glass.arena.task.GlassFireworkTask;
import com.covercorp.kaelaevent.minigame.games.glass.arena.task.GlassPreLobbyTask;
import com.covercorp.kaelaevent.minigame.games.glass.arena.task.GlassTickTask;
import com.covercorp.kaelaevent.minigame.games.glass.arena.task.GlassTimeTask;
import com.covercorp.kaelaevent.minigame.games.glass.inventory.GlassItemCollection;
import com.covercorp.kaelaevent.minigame.games.glass.player.GlassPlayer;
import com.covercorp.kaelaevent.minigame.games.glass.team.GlassTeam;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import com.covercorp.kaelaevent.util.ZoneCuboid;
import com.covercorp.kaelaevent.util.simple.Pair;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;

@Getter(AccessLevel.PUBLIC)
public final class GlassArena {
    private final GlassMiniGame glassMiniGame;

    private final PlayerHelper<GlassMiniGame> playerHelper;
    private final TeamHelper<GlassMiniGame, GlassPlayer> teamHelper;
    private final Announcer<GlassMiniGame> announcer;

    private final MiniMessage gameMiniMessage;
    private final GlassTimeBarHelper timeBarHelper;

    private final Location lobbyLocation;
    private final Location arenaSpawnLocation;
    private final GlassMatchProperties glassMatchProperties;
    private final Map<String, BridgePart> bridgeParts;
    private final ZoneCuboid finishZone;

    @Setter(AccessLevel.PUBLIC) private int gameTime;

    private final int timeLimit;
    private static final int DEFAULT_TIME_LIMIT = 90;

    @Setter(AccessLevel.PUBLIC) private GlassMatchState state;

    @Setter(AccessLevel.PUBLIC) private GlassTeam crossingTeam;

    public GlassArena(GlassMiniGame glassMiniGame) {
        this.glassMiniGame = glassMiniGame;

        playerHelper = glassMiniGame.getPlayerHelper();
        teamHelper = glassMiniGame.getTeamHelper();
        announcer = glassMiniGame.getAnnouncer();

        gameMiniMessage = glassMiniGame.getMiniMessage();

        timeBarHelper = new GlassTimeBarHelper(this);

        lobbyLocation = glassMiniGame.getConfigHelper().getLobbySpawn();
        arenaSpawnLocation = glassMiniGame.getConfigHelper().getArenaSpawn();
        glassMatchProperties = new GlassMatchProperties(this);

        bridgeParts = new ConcurrentHashMap<>();

        getGlassMiniGame().getConfigHelper().getGlasses().forEach(glassId -> {
            final Pair<Location, Location> locationPair = getGlassMiniGame().getConfigHelper().getGlassSides(glassId);
            final BridgePart bridgePart = new BridgePart(glassId, locationPair.key(), locationPair.value());

            bridgeParts.put(bridgePart.getId(), bridgePart);
        });

        finishZone = glassMiniGame.getConfigHelper().getFinishZone();
        timeLimit = DEFAULT_TIME_LIMIT;
        crossingTeam = null;

        Bukkit.getServer().getPluginManager().registerEvents(new GlassMatchListener(this), getGlassMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new GlassMatchGameListener(this), getGlassMiniGame().getKaelaEvent());

        setState(GlassMatchState.WAITING);
    }

    public void start() {
        Bukkit.getScheduler().cancelTask(getGlassMatchProperties().getStartingTaskId());

        this.glassMatchProperties.setStartingTaskId(0);

        setCrossingTeam(this.teamHelper.getTeamList().stream().map(generic -> (GlassTeam)generic).toList().getFirst());
        clearOldGlasses();
        setGameTime(0);

        resetGlasses();

        this.playerHelper.getPlayerList().forEach(participant -> {
            Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null)
                return;
            GlassTeam team = (GlassTeam)participant.getMiniGameTeam();
            if (team == null)
                return;
            player.teleport(getArenaSpawnLocation());
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setFlying(false);
        });

        announcer.sendGlobalTitle(Title.title(this.gameMiniMessage
                        .deserialize("<gold><bold>GLASS BRIDGE CROSS"), this.gameMiniMessage
                        .deserialize("<gray>Get ready!"),
                Title.Times.times(
                        Duration.ofMillis(0L),
                        Duration.ofMillis(2000L),
                        Duration.ofMillis(1000L))));
        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lThe Glass Bridge", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lJump over the glass bridge to win.", true);
        announcer.sendGlobalMessage("&e&lOne of the crystals will break when you jump on it", true);
        announcer.sendGlobalMessage("&e&lAt least one talent of the team must cross all", true);
        announcer.sendGlobalMessage("&e&lthe glass bridge to get the victory!", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        glassMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(this.glassMiniGame.getKaelaEvent(), new GlassPreLobbyTask(this), 0L, 20L).getTaskId()
        );

        setState(GlassMatchState.ARENA_STARTING);
    }

    public void postStart() {
        glassMatchProperties.setPreLobby(false);

        Bukkit.getScheduler().cancelTask(this.glassMatchProperties.getPreLobbyTaskId());

        glassMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getGlassMiniGame().getKaelaEvent(), new GlassTickTask(this), 0L, 1L).getTaskId()
        );
        glassMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getGlassMiniGame().getKaelaEvent(), new GlassTimeTask(this), 0L, 20L).getTaskId()
        );

        timeBarHelper.start();

        announcer.sendGlobalMessage("&eStart crossing!", false);
        announcer.sendGlobalTitle(Title.title(this.gameMiniMessage
                        .deserialize("<gold><bold>Start!"),
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(0L),
                        Duration.ofMillis(1000L),
                        Duration.ofMillis(500L))));
        announcer.sendGlobalSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0F, 2.0F);

        resetGlasses();

        this.playerHelper.getPlayerList().forEach(participant -> {
            Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null)
                return;
            GlassTeam team = (GlassTeam)participant.getMiniGameTeam();
            if (team == null)
                return;
            player.teleport(getArenaSpawnLocation());
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setFlying(false);

            final AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            final AttributeModifier speedModifier = new AttributeModifier(new NamespacedKey(KaelaEvent.getKaelaEvent(), "speed_attr"), 0.03324, AttributeModifier.Operation.ADD_NUMBER);
            if (speedAttribute != null) {
                speedAttribute.addModifier(speedModifier);
            }
            final AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
            final AttributeModifier jumpModifier = new AttributeModifier(new NamespacedKey(KaelaEvent.getKaelaEvent(), "jump_attr"), 0.253, AttributeModifier.Operation.ADD_NUMBER);
            if (jumpAttribute != null) {
                jumpAttribute.addModifier(jumpModifier);
            }
        });

        setState(GlassMatchState.GAME);
    }

    public void stop() {
        setState(GlassMatchState.ENDING);

        stopTasks();
        timeBarHelper.stop();

        if (crossingTeam == null) {
            announcer.sendGlobalMessage(" \n&6&lThe game ended without a winner...", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);
        } else {
            int alivePlayers = this.crossingTeam.getPlayers().stream().filter(glassPlayer -> !glassPlayer.isDead()).toList().size();
            
            if (alivePlayers <= 0) {
                announcer.sendGlobalTitle(Title.title(this.gameMiniMessage
                                .deserialize("<red>Game Over!"), this.gameMiniMessage
                                .deserialize("<gray>All talents are dead!"),
                        Title.Times.times(Duration.ZERO,

                                Duration.ofSeconds(3L),
                                Duration.ofSeconds(1L))));
                announcer.sendGlobalSound(Sound.ENTITY_WITHER_DEATH, 0.7F, 0.7F);
                announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
                announcer.sendGlobalMessage("&e&lMatch ended!", true);
                announcer.sendGlobalMessage(" ", false);
                announcer.sendGlobalMessage("&c&lAll talents of &f" + LegacyComponentSerializer.legacyAmpersand().serialize(crossingTeam.getBetterPrefix()) + "&c&lare dead!", true);
                announcer.sendGlobalMessage(" ", false);
                announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
            } else {
                announcer.sendGlobalTitle(
                        Title.title(gameMiniMessage.deserialize(
                                "<green>Game ended!"), 
                                crossingTeam.getBetterPrefix().append(gameMiniMessage.deserialize("<gray>crossed the bridge!")),
                        Title.Times.times(Duration.ofMillis(0L), Duration.ofMillis(5000L), Duration.ofMillis(1000L)))
                );
                
                announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
                announcer.sendGlobalMessage("&e&lMatch ended!", true);
                announcer.sendGlobalMessage(" ", false);
                announcer.sendGlobalMessage("&eWinner team: &f" + LegacyComponentSerializer.legacyAmpersand().serialize(crossingTeam.getBetterPrefix()), true);
                announcer.sendGlobalMessage(this.crossingTeam.getPlayers().stream().map(MiniGamePlayer::getName).collect(Collectors.joining("&f & ")), true);
                announcer.sendGlobalMessage(" ", false);
                announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
                
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 0.8F));
                
                glassMatchProperties.setFireworkTaskId(
                        Bukkit.getScheduler().runTaskTimer(getGlassMiniGame().getKaelaEvent(), new GlassFireworkTask(this, this.crossingTeam), 0L, 20L).getTaskId()
                );
                
                Bukkit.getScheduler().runTaskLater(getGlassMiniGame().getKaelaEvent(), () -> {
                    Bukkit.getScheduler().cancelTask(glassMatchProperties.getFireworkTaskId());
                    
                    glassMatchProperties.setFireworkTaskId(0);
                }, 20L * 3L);
            }
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getGlassMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (GlassPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.clearActivePotionEffects();
                player.setFlying(true);

                GlassItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
            });

            // Reset properties
            crossingTeam = null;
            glassMatchProperties.resetTimer();

            setState(GlassMatchState.WAITING);
        }, 120L);
    }

    public void checkLoser() {
        if (getAlivePlayers() <= 0) stop();
    }

    public void breakBridge() {
        getBridgeParts().forEach((id, part) -> part.getGlasses().forEach(part::breakGlass));

        Bukkit.getScheduler().runTaskLater(glassMiniGame.getKaelaEvent(), () -> {
            if (this.getState() == GlassMatchState.GAME) {
                this.getPlayerHelper().getPlayerList().forEach(genericPlayer -> {
                    Player player = Bukkit.getPlayer(genericPlayer.getUniqueId());
                    if (player != null) {
                        if (player.getGameMode() != GameMode.SPECTATOR) {
                            player.setGameMode(GameMode.SPECTATOR);
                        }
                    }
                });
                Bukkit.getScheduler().runTaskLater(glassMiniGame.getKaelaEvent(), () -> {
                    if (this.getState() == GlassMatchState.GAME) {
                        int alivePlayers = this.crossingTeam.getPlayers().stream().filter(glassPlayer -> !glassPlayer.isDead()).toList().size();
                        if (alivePlayers < 1) {
                            this.stop();
                        }
                    }
                }, 5L);
            }
        }, 75L);
    }

    public int getAlivePlayers() {
        if (crossingTeam == null) return 0;
        
        return this.crossingTeam.getPlayers().stream().filter(glassPlayer -> !glassPlayer.isDead()).toList().size();
    }

    public void resetGlasses() {
        bridgeParts.forEach((id, part) -> part.resetGlass());
        
        announcer.sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
        announcer.sendGlobalMessage("&7[!] The bridge has been reset.", false);
    }

    public void clearOldGlasses() {
        this.arenaSpawnLocation.getWorld().getEntitiesByClass(ItemDisplay.class)
                .stream().filter(entity -> NBTMetadataUtil.hasEntityString(entity, "bridge_glass"))
                .forEach(Entity::remove);
        this.arenaSpawnLocation.getWorld().getEntitiesByClass(Interaction.class)
                .stream().filter(entity -> NBTMetadataUtil.hasEntityString(entity, "bridge_glass"))
                .forEach(Entity::remove);
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(this.glassMatchProperties.getStartingTaskId());
        this.glassMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(this.glassMatchProperties.getPreLobbyTaskId());
        this.glassMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(this.glassMatchProperties.getArenaTickTaskId());
        this.glassMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(this.glassMatchProperties.getArenaTimeTaskId());
        this.glassMatchProperties.setArenaTimeTaskId(0);
    }

    public int getTimeLeft() {
        return this.timeLimit - this.gameTime;
    }
}