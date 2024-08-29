package com.covercorp.kaelaevent.minigame.games.tug.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.tug.arena.task.*;
import com.covercorp.kaelaevent.minigame.games.tug.player.TugPlayer;
import com.covercorp.kaelaevent.minigame.games.tug.team.TugTeam;
import com.covercorp.kaelaevent.minigame.games.tug.TugMiniGame;
import com.covercorp.kaelaevent.minigame.games.tug.arena.bar.TimeBarHelper;
import com.covercorp.kaelaevent.minigame.games.tug.arena.listener.TugMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.tug.arena.listener.TugMatchListener;
import com.covercorp.kaelaevent.minigame.games.tug.arena.properties.TugMatchProperties;
import com.covercorp.kaelaevent.minigame.games.tug.arena.state.TugMatchState;
import com.covercorp.kaelaevent.minigame.games.tug.inventory.TugItemCollection;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
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
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
public final class TugArena {
    private final TugMiniGame tugMiniGame;

    private final PlayerHelper<TugMiniGame> playerHelper;
    private final TeamHelper<TugMiniGame, TugPlayer> teamHelper;
    private final Announcer<TugMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final TimeBarHelper timeBarHelper;

    private final Location lobbyLocation;
    private final Location centerLocation;
    private final List<Location> teamSpawnSpots;

    private final TugMatchProperties tugMatchProperties;

    private final int timeLimit;
    private final static int DEFAULT_TIME_LIMIT = 60; //60 * 2;

    @Setter(AccessLevel.PUBLIC) private int gameTime;
    @Setter(AccessLevel.PUBLIC) private TugMatchState state;

    private TugTeam possibleWinnerTeam;

    public TugArena(final TugMiniGame tugMiniGame) {
        this.tugMiniGame = tugMiniGame;

        playerHelper = tugMiniGame.getPlayerHelper();
        teamHelper = tugMiniGame.getTeamHelper();
        announcer = tugMiniGame.getAnnouncer();

        gameMiniMessage = tugMiniGame.getMiniMessage();

        timeBarHelper = new TimeBarHelper(this);

        lobbyLocation = tugMiniGame.getConfigHelper().getLobbySpawn();
        centerLocation = tugMiniGame.getConfigHelper().getCenter();

        teamSpawnSpots = new ArrayList<>();
        teamSpawnSpots.addAll(tugMiniGame.getConfigHelper().getTeamSpawns());

        tugMatchProperties = new TugMatchProperties(this);

        timeLimit = DEFAULT_TIME_LIMIT;

        Bukkit.getServer().getPluginManager().registerEvents(new TugMatchListener(this), getTugMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new TugMatchGameListener(this), getTugMiniGame().getKaelaEvent());

        setState(TugMatchState.WAITING);
    }

    public void start() {
        // Cancel the match start item task
        Bukkit.getScheduler().cancelTask(getTugMatchProperties().getStartingTaskId());
        tugMatchProperties.setStartingTaskId(0);

        setGameTime(0);

        final Iterator<Location> spawnIterator = teamSpawnSpots.iterator();
        playerHelper.getPlayerList().forEach(participant -> {
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) return;

            final TugTeam team = (TugTeam) participant.getMiniGameTeam();
            if (team == null) return;

            // Get the next spawn in getTeamSpawns, spawns must not be the same for every player
            if (spawnIterator.hasNext()) {
                final Location teamSpawn = spawnIterator.next();
                team.setSpawnPoint(teamSpawn);
                player.teleport(team.getSpawnPoint());
            }

            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
        });

        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>TUG OF WAR"),
                gameMiniMessage.deserialize("<gray>Get ready!"),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(1000)
                )
        ));

        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);

        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lTug of War", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lRIGHT click the rope to pull it and earn score.", true);
        announcer.sendGlobalMessage("&e&lThe first Talent whom gets a 100 score advantage", true);
        announcer.sendGlobalMessage("&e&lover the opponent wins.", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        // The task constructor has the setPreLobby(true) fu    nc
        tugMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(tugMiniGame.getKaelaEvent(), new TugPreLobbyTask(this), 0L, 20L).getTaskId()
        );
        setState(TugMatchState.ARENA_STARTING);
    }

    public void postStart() {
        // Cancel the previous task
        tugMatchProperties.setPreLobby(false);
        Bukkit.getScheduler().cancelTask(tugMatchProperties.getPreLobbyTaskId());

        tugMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getTugMiniGame().getKaelaEvent(), new TugTickTask(this), 0L, 1L).getTaskId()
        );
        tugMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getTugMiniGame().getKaelaEvent(), new TugTimeTask(this), 0L, 20L).getTaskId()
        );

        timeBarHelper.start();

        announcer.sendGlobalMessage("&eRopes enabled! PULL!", false);
        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>PULL!"),
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(1000),
                        Duration.ofMillis(500)
                )
        ));
        announcer.sendGlobalSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0F, 2.0F);

        setState(TugMatchState.GAME);
    }

    public void stop() {
        setState(TugMatchState.ENDING);

        stopTasks();
        timeBarHelper.stop();

        if (possibleWinnerTeam == null) {
            announcer.sendGlobalMessage(" \n&6&lThe game ended without a winner... let's try again!", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);
        } else {
            announcer.sendGlobalTitle(Title.title(
                    gameMiniMessage.deserialize("<green>Game ended!"),
                    gameMiniMessage.deserialize(
                            possibleWinnerTeam.getPlayers().stream().map(TugPlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")) + " <gray>won the match!"
                    ),
                    Title.Times.times(
                            Duration.ofMillis(0),
                            Duration.ofMillis(5000),
                            Duration.ofMillis(1000)
                    )
            ));

            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
            announcer.sendGlobalMessage("&e&lMatch ended!", true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&eWinner team: &f" + LegacyComponentSerializer.legacyAmpersand().serialize(possibleWinnerTeam.getBetterPrefix()), true);
            announcer.sendGlobalMessage(possibleWinnerTeam.getPlayers().stream().map(TugPlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")), true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 0.8F));

            tugMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getTugMiniGame().getKaelaEvent(), new TugFireworkTask(this, possibleWinnerTeam), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getTugMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(tugMatchProperties.getFireworkTaskId());
                tugMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getTugMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (TugPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);

                TugItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
            });

            // Reset properties
            possibleWinnerTeam = null;
            tugMatchProperties.resetTimer();

            setState(TugMatchState.WAITING);
        }, 120L);
    }

    public void runLoser() {
        setState(TugMatchState.ENDING);

        final TugTeam team1 = (TugTeam) teamHelper.getTeamList().get(0);
        final Set<TugPlayer> team1Players = team1.getPlayers();
        int team1AlivePlayers = 0;
        for (final TugPlayer tugPlayer : team1Players) {
            if (!tugPlayer.isSpectating()) team1AlivePlayers++;
        }

        final TugTeam team2 = (TugTeam) teamHelper.getTeamList().get(1);
        final Set<TugPlayer> team2Players = team2.getPlayers();
        int team2AlivePlayers = 0;
        for (final TugPlayer tugPlayer : team2Players) {
            if (!tugPlayer.isSpectating()) team2AlivePlayers++;
        }

        // Check if some team has 0 players alive
        if (team1AlivePlayers == 0) {
            possibleWinnerTeam = team2;
            announcer.sendGlobalMessage(
                    LegacyComponentSerializer.legacyAmpersand().serialize(team2.getBetterPrefix()) +
                            "&6won because &f" +
                            LegacyComponentSerializer.legacyAmpersand().serialize(team1.getBetterPrefix()) +
                            "&6has no players alive!",
                    false);
        } else if (team2AlivePlayers == 0) {
            possibleWinnerTeam = team1;
            announcer.sendGlobalMessage(
                    LegacyComponentSerializer.legacyAmpersand().serialize(team1.getBetterPrefix()) +
                            "&6won because &f" +
                            LegacyComponentSerializer.legacyAmpersand().serialize(team2.getBetterPrefix()) +
                            "&6has no players alive!",
                    false);
        } else {
            if (team1.getTeamScore() == team2.getTeamScore()) {
                if (team1AlivePlayers == team2AlivePlayers) {
                    possibleWinnerTeam = null;
                    announcer.sendGlobalMessage(
                            "&cThere's no winner because both teams have the same alive talents and the score is a tie...",
                            false);
                } else if (team1AlivePlayers > team2AlivePlayers) {
                    possibleWinnerTeam = team1;
                    announcer.sendGlobalMessage(
                            LegacyComponentSerializer.legacyAmpersand().serialize(team1.getBetterPrefix()) +
                                    "&6won because &f" +
                                    LegacyComponentSerializer.legacyAmpersand().serialize(team2.getBetterPrefix()) +
                                    "&6has more players alive and the score is a tie!",
                            false);
                } else {
                    possibleWinnerTeam = team2;
                    announcer.sendGlobalMessage(
                            LegacyComponentSerializer.legacyAmpersand().serialize(team2.getBetterPrefix()) +
                                    "&6won because &f" +
                                    LegacyComponentSerializer.legacyAmpersand().serialize(team1.getBetterPrefix()) +
                                    "&6has more players alive and the score is a tie!",
                            false);
                }
            }
        }

        final List<TugTeam> tugTeams = teamHelper.getTeamList().stream()
                .filter(TugTeam.class::isInstance)
                .map(TugTeam.class::cast)
                .sorted(Comparator.comparingInt(TugTeam::getTeamScore).reversed())
                .toList();

        // Still no winner
        if (possibleWinnerTeam == null) {
            TugTeam winnerTeam;

            if (tugTeams.isEmpty()) {
                winnerTeam = null;
            } else {
                winnerTeam = tugTeams.getFirst();
            }

            if (winnerTeam == null) {
                announcer.sendGlobalMessage(" \n&6&lThere's no winner team? This should not be happening?", false);
                announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);

                stop();

                return;
            }

            possibleWinnerTeam = winnerTeam;
        }

        final TugTeam loserTeam = (TugTeam) teamHelper.getTeamList().stream().filter(t -> !t.getIdentifier().equals(possibleWinnerTeam.getIdentifier())).findFirst().orElse(null);

        if (loserTeam == null) {
            announcer.sendGlobalMessage(" \n&6&lThere's no loser team? This should not be happening?", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);

            stop();
            return;
        }

        Bukkit.getScheduler().runTask(getTugMiniGame().getKaelaEvent(), new TugLoserKillTask(this, loserTeam));
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(tugMatchProperties.getStartingTaskId());
        tugMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(tugMatchProperties.getPreLobbyTaskId());
        tugMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(tugMatchProperties.getArenaTickTaskId());
        tugMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(tugMatchProperties.getArenaTimeTaskId());
        tugMatchProperties.setArenaTimeTaskId(0);
    }

    public int getTimeLeft() {
        return timeLimit - gameTime;
    }
}
