package com.covercorp.kaelaevent.minigame.games.squid.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.squid.SquidMiniGame;
import com.covercorp.kaelaevent.minigame.games.squid.arena.bar.SquidTimeBarHelper;
import com.covercorp.kaelaevent.minigame.games.squid.arena.event.SquidGalonChanStartAnalyzingEvent;
import com.covercorp.kaelaevent.minigame.games.squid.arena.galon.GalonChan;
import com.covercorp.kaelaevent.minigame.games.squid.arena.galon.status.GalonChanStatus;
import com.covercorp.kaelaevent.minigame.games.squid.arena.listener.SquidMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.squid.arena.listener.SquidMatchListener;
import com.covercorp.kaelaevent.minigame.games.squid.arena.properties.SquidMatchProperties;
import com.covercorp.kaelaevent.minigame.games.squid.arena.state.SquidMatchState;
import com.covercorp.kaelaevent.minigame.games.squid.arena.task.SquidFireworkTask;
import com.covercorp.kaelaevent.minigame.games.squid.arena.task.SquidPreLobbyTask;
import com.covercorp.kaelaevent.minigame.games.squid.arena.task.SquidTickTask;
import com.covercorp.kaelaevent.minigame.games.squid.arena.task.SquidTimeTask;
import com.covercorp.kaelaevent.minigame.games.squid.inventory.SquidItemCollection;
import com.covercorp.kaelaevent.minigame.games.squid.player.SquidPlayer;
import com.covercorp.kaelaevent.minigame.games.squid.team.SquidTeam;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import com.covercorp.kaelaevent.util.TimeUtils;
import com.covercorp.kaelaevent.util.ZoneCuboid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
public final class SquidArena {
    private final SquidMiniGame squidMiniGame;

    private final PlayerHelper<SquidMiniGame> playerHelper;
    private final TeamHelper<SquidMiniGame, SquidPlayer> teamHelper;
    private final Announcer<SquidMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final Location lobbyLocation;
    private final List<Location> arenaSpawnLocations;
    private final List<Location> arenaGunLocations;
    private final Location galonChanLocation;
    private final ZoneCuboid startZone;
    private final ZoneCuboid goalZone;

    private final SquidMatchProperties squidMatchProperties;

    private final SquidTimeBarHelper timeBarHelper;

    private final GalonChan galonChan;

    private final int timeLimit;
    private final static int DEFAULT_TIME_LIMIT = 80; //60 * 2;

    @Setter(AccessLevel.PUBLIC) private int gameTime;
    @Setter(AccessLevel.PUBLIC) private SquidMatchState state;

    private final Queue<UUID> movedTalents;
    private List<SquidTeam> winnerResults;

    public SquidArena(final SquidMiniGame squidMiniGame) {
        this.squidMiniGame = squidMiniGame;

        playerHelper = squidMiniGame.getPlayerHelper();
        teamHelper = squidMiniGame.getTeamHelper();
        announcer = squidMiniGame.getAnnouncer();

        gameMiniMessage = squidMiniGame.getMiniMessage();

        lobbyLocation = squidMiniGame.getConfigHelper().getLobbySpawn();
        arenaSpawnLocations = squidMiniGame.getConfigHelper().getArenaSpawns();
        arenaGunLocations = squidMiniGame.getConfigHelper().getGunSpawns();
        galonChanLocation = squidMiniGame.getConfigHelper().getGalonChanSpawn();
        startZone = squidMiniGame.getConfigHelper().getStartZone();
        goalZone = squidMiniGame.getConfigHelper().getGoalZone();

        squidMatchProperties = new SquidMatchProperties(this);

        timeBarHelper = new SquidTimeBarHelper(this);

        galonChan = new GalonChan(this);

        timeLimit = DEFAULT_TIME_LIMIT; // 2 mins
        
        Bukkit.getServer().getPluginManager().registerEvents(new SquidMatchListener(this), getSquidMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new SquidMatchGameListener(this), getSquidMiniGame().getKaelaEvent());

        movedTalents = new LinkedList<>();
        winnerResults = List.of();
        clearGalonChan();

        setState(SquidMatchState.WAITING);
    }

    public void start() {
        // Cancel the match start item task
        Bukkit.getScheduler().cancelTask(getSquidMatchProperties().getStartingTaskId());
        squidMatchProperties.setStartingTaskId(0);

        squidMatchProperties.setFinalAnalyze(false);

        setGameTime(0);
        clearGalonChan();

        getGalonChan().spawn();
        getStartZone().getBlockList().forEachRemaining(block -> block.setType(Material.BARRIER));

        this.playerHelper.getPlayerList().forEach(participant -> {
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) return;
            final SquidTeam team = (SquidTeam)participant.getMiniGameTeam();
            if (team == null) return;

            player.teleport(getArenaSpawnLocations().get((new Random()).nextInt(getArenaSpawnLocations().size())));
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setPose(Pose.STANDING, false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50000, 0));
        });

        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>GREEN LIGHT RED LIGHT"),
                gameMiniMessage.deserialize("<gray>Get ready!"),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(1000)
                )
        ));

        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);

        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lGreen Light, Red Light", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lBeware of Galon-chan witnessing you move", true);
        announcer.sendGlobalMessage("&e&lif they don't want you to do so!", true);
        announcer.sendGlobalMessage("&e&lRun towards Galon-chan whenever their light", true);
        announcer.sendGlobalMessage("&e&lis GREEN. If light is RED, you MUST stop.", true);
        announcer.sendGlobalMessage("&e&lThe team with most talents alive wins the game.", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        // The task constructor has the setPreLobby(true) func
        squidMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(squidMiniGame.getKaelaEvent(), new SquidPreLobbyTask(this), 0L, 20L).getTaskId()
        );
        setState(SquidMatchState.ARENA_STARTING);
    }

    public void postStart() {
        // Cancel the previous task
        squidMatchProperties.setPreLobby(false);
        Bukkit.getScheduler().cancelTask(squidMatchProperties.getPreLobbyTaskId());

        squidMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getSquidMiniGame().getKaelaEvent(), new SquidTickTask(this), 0L, 1L).getTaskId()
        );
        squidMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getSquidMiniGame().getKaelaEvent(), new SquidTimeTask(this), 0L, 20L).getTaskId()
        );

        getTimeBarHelper().start();
        getStartZone().getBlockList().forEachRemaining(block -> block.setType(Material.AIR));
        getAnnouncer().sendGlobalSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0F, 2.0F);

        getSquidMatchProperties().setAnalyzing(false);
        getGalonChan().setStatus(GalonChanStatus.YES);
        getSquidMatchProperties().setStartTime(Instant.now());

        setState(SquidMatchState.GAME);
    }

    public void stop() {
        setState(SquidMatchState.ENDING);

        stopTasks();

        getTimeBarHelper().stop();

        if (this.winnerResults.isEmpty()) {
            announcer.sendGlobalTitle(Title.title(this.gameMiniMessage
                            .deserialize("<red>Game Over!"), this.gameMiniMessage
                            .deserialize("<gray>All talents are dead!"),
                    Title.Times.times(Duration.ofMillis(0L), Duration.ofMillis(5000L), Duration.ofMillis(1000L))));
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
            announcer.sendGlobalMessage("&e&lMatch ended!", true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&c&lAll talents are dead, there's no winner.", true);
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.ENTITY_WITHER_DEATH, 0.8F, 0.8F));
        } else {
            announcer.sendGlobalTitle(Title.title(gameMiniMessage.deserialize(
                    "<green>Game ended!"),
                    winnerResults.getFirst().getBetterPrefix()
                            .append(gameMiniMessage.deserialize("<gray>won the match!")),
                    Title.Times.times(Duration.ofMillis(0L), Duration.ofMillis(5000L), Duration.ofMillis(1000L)))
            );

            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
            announcer.sendGlobalMessage("&e&lMatch ended!", true);
            announcer.sendGlobalMessage("&eWinner team: &f" + LegacyComponentSerializer.legacyAmpersand().serialize(winnerResults.getFirst().getBetterPrefix()), true);
            announcer.sendGlobalMessage((winnerResults.getFirst()).getPlayers().stream().map(MiniGamePlayer::getName).collect(Collectors.joining("&f & ")), true);
            announcer.sendGlobalMessage(" ", false);
            int place = 1;
            for (SquidTeam team : this.winnerResults) {
                final int alivePlayers = team.getAlivePlayers();
                final int finishedPlayers = team.getFinishedPlayers();
                final String time = TimeUtils.formatTime((int)team.getAverageFinishTime(this.squidMatchProperties.getStartTime()));

                announcer.sendGlobalMessage("&a#" + place + ". &f" +
                        LegacyComponentSerializer.legacyAmpersand().serialize(team.getBetterPrefix()) + "&b[" + alivePlayers + " talents alive] " + ((
                        alivePlayers >= 1 && finishedPlayers >= 1) ? ("&f[" + time + " average time]") : "&f[No time]"), false);
                place++;
            }
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 0.8F));

            squidMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getSquidMiniGame().getKaelaEvent(), new SquidFireworkTask(this, winnerResults.getFirst()), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getSquidMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(squidMatchProperties.getFireworkTaskId());
                squidMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getSquidMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (SquidPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setPose(Pose.STANDING, false);

                SquidItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
            });

            // Reset properties
            winnerResults = List.of();
            getSquidMatchProperties().resetTimer();
            getGalonChan().deSpawn();
            clearGalonChan();

            setState(SquidMatchState.WAITING);
        }, 120L);
    }

    public void endGame() {
        if (getState() != SquidMatchState.GAME) return;

        if (!squidMatchProperties.isFinalAnalyze() && !getUnfinishedAlivePlayers().isEmpty()) {
            final List<SquidPlayer> playersToShot = getUnfinishedAlivePlayers();

            playersToShot.forEach(player -> player.setFlaggedToDeath(true));

            // Add every player to the shot list
            movedTalents.addAll(playersToShot.stream().map(MiniGamePlayer::getUniqueId).toList());

            // Make galon chan analyze them
            squidMatchProperties.setFinalAnalyze(true);
            Bukkit.getPluginManager().callEvent(new SquidGalonChanStartAnalyzingEvent(this));

            return;
        }

        if (getFinishedPlayers() <= 0) {
            winnerResults = List.of();
        } else {
            winnerResults = this.teamHelper
                    .getTeamList()
                    .stream()
                    .filter(SquidTeam.class::isInstance)
                    .map(SquidTeam.class::cast)
                    .sorted(Comparator.comparingDouble(squidTeam -> squidTeam.getAverageFinishTime(this.squidMatchProperties.getStartTime())))
                    .toList();
        }
        stop();
    }

    public void clearGalonChan() {
        this.lobbyLocation
                .getWorld()
                .getEntitiesByClass(ItemDisplay.class)
                .stream()
                .filter(entity -> NBTMetadataUtil.hasEntityString(entity, "galon_chan"))
                .forEach(Entity::remove);
        this.lobbyLocation
                .getWorld()
                .getEntitiesByClass(Interaction.class)
                .stream()
                .filter(entity -> NBTMetadataUtil.hasEntityString(entity, "galon_chan"))
                .forEach(Entity::remove);
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(squidMatchProperties.getStartingTaskId());
        squidMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(squidMatchProperties.getPreLobbyTaskId());
        squidMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(squidMatchProperties.getArenaTickTaskId());
        squidMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(squidMatchProperties.getArenaTimeTaskId());
        squidMatchProperties.setArenaTimeTaskId(0);
    }

    public int getTimeLeft() {
        return timeLimit - gameTime;
    }

    public List<SquidPlayer> getUnfinishedAlivePlayers() {
        return getPlayerHelper()
                .getPlayerList()
                .stream()
                .map(g -> (SquidPlayer)g)
                .filter(squidPlayer -> !squidPlayer.isDead())
                .filter(squidPlayer -> !squidPlayer.isFinished())
                .toList();
    }

    public int getFinishedPlayers() {
        return getPlayerHelper()
                .getPlayerList()
                .stream()
                .map(g -> (SquidPlayer)g)
                .filter(SquidPlayer::isFinished)
                .toList()
                .size();
    }

    public int getDeadPlayers() {
        return getPlayerHelper()
                .getPlayerList()
                .stream()
                .map(g -> (SquidPlayer)g)
                .filter(squidPlayer -> !squidPlayer.isFinished())
                .filter(SquidPlayer::isDead)
                .toList()
                .size();
    }
}
