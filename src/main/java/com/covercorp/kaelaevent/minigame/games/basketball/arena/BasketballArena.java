package com.covercorp.kaelaevent.minigame.games.basketball.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.basketball.BasketballMiniGame;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.ball.ShootedBasketball;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.listener.BasketballMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.listener.BasketballMatchListener;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.properties.BasketballMatchProperties;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.scoreboard.BasketballScoreboardHelper;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.state.BasketballMatchState;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.task.BasketballFireworkTask;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.task.BasketballPreLobbyTask;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.task.BasketballTickTask;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.task.BasketballTimeTask;
import com.covercorp.kaelaevent.minigame.games.basketball.inventory.BasketballItemCollection;
import com.covercorp.kaelaevent.minigame.games.basketball.player.BasketballPlayer;
import com.covercorp.kaelaevent.minigame.games.basketball.team.BasketballTeam;
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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
public final class BasketballArena {
    private final BasketballMiniGame basketballMiniGame;

    private final PlayerHelper<BasketballMiniGame> playerHelper;
    private final TeamHelper<BasketballMiniGame, BasketballPlayer> teamHelper;
    private final Announcer<BasketballMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final BasketballScoreboardHelper scoreboardHelper;

    private final Location lobbyLocation;
    private final Location arenaSpawnLocation;

    private final Location hitBoxLocation;

    private final BasketballMatchProperties basketballMatchProperties;

    private final int timeLimit;
    private final static int DEFAULT_TIME_LIMIT = 90; //60 * 2;

    @Setter(AccessLevel.PUBLIC) private int gameTime;
    @Setter(AccessLevel.PUBLIC) private BasketballMatchState state;

    private final List<Location> basketballSpawnSpots;
    private final Map<UUID, ShootedBasketball> shootedBasketballs;

    private BasketballTeam possibleWinnerTeam;

    public BasketballArena(final BasketballMiniGame basketballMiniGame) {
        this.basketballMiniGame = basketballMiniGame;

        playerHelper = basketballMiniGame.getPlayerHelper();
        teamHelper = basketballMiniGame.getTeamHelper();
        announcer = basketballMiniGame.getAnnouncer();

        gameMiniMessage = basketballMiniGame.getMiniMessage();

        scoreboardHelper = new BasketballScoreboardHelper(this);

        lobbyLocation = basketballMiniGame.getConfigHelper().getLobbySpawn();
        arenaSpawnLocation = basketballMiniGame.getConfigHelper().getArenaSpawn();

        hitBoxLocation = basketballMiniGame.getConfigHelper().getBasketHitboxLocation();

        basketballMatchProperties = new BasketballMatchProperties(this);

        timeLimit = DEFAULT_TIME_LIMIT; // 2 mins

        basketballSpawnSpots = new ArrayList<>();
        basketballSpawnSpots.addAll(basketballMiniGame.getConfigHelper().getBallSpawns());

        shootedBasketballs = new HashMap<>();

        Bukkit.getServer().getPluginManager().registerEvents(new BasketballMatchListener(this), getBasketballMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new BasketballMatchGameListener(this), getBasketballMiniGame().getKaelaEvent());

        setState(BasketballMatchState.WAITING);
    }

    public void start() {
        // Cancel the match start item task
        Bukkit.getScheduler().cancelTask(getBasketballMatchProperties().getStartingTaskId());
        basketballMatchProperties.setStartingTaskId(0);
        basketballMatchProperties.setBallSpawnCooldown(10);

        setGameTime(0);

        playerHelper.getPlayerList().forEach(participant -> {
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) return;

            final BasketballTeam team = (BasketballTeam) participant.getMiniGameTeam();
            if (team == null) return;

            player.teleport(getArenaSpawnLocation());
            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(false);
            player.setFlying(false);
        });

        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>BASKETBALL SHOOTERS"),
                gameMiniMessage.deserialize("<gray>Get ready!"),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(1000)
                )
        ));

        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);

        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lBasketball Shooters", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lUse your Basketstopper to shoot basketballs", true);
        announcer.sendGlobalMessage("&e&linto the goal zone and get score points.", true);
        announcer.sendGlobalMessage("&e&lThe Talent with most score points wins!", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        // The task constructor has the setPreLobby(true) func
        basketballMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(basketballMiniGame.getKaelaEvent(), new BasketballPreLobbyTask(this), 0L, 20L).getTaskId()
        );
        setState(BasketballMatchState.ARENA_STARTING);
    }

    public void postStart() {
        // Cancel the previous task
        basketballMatchProperties.setPreLobby(false);
        Bukkit.getScheduler().cancelTask(basketballMatchProperties.getPreLobbyTaskId());

        basketballMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getBasketballMiniGame().getKaelaEvent(), new BasketballTickTask(this), 0L, 1L).getTaskId()
        );
        basketballMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getBasketballMiniGame().getKaelaEvent(), new BasketballTimeTask(this), 0L, 20L).getTaskId()
        );

        announcer.sendGlobalMessage("&eBasketstoppers enabled! Time to score!", false);
        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>SHOOT!"),
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(1000),
                        Duration.ofMillis(500)
                )
        ));
        announcer.sendGlobalSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0F, 2.0F);

        playerHelper.getPlayerList().forEach(genericPlayer -> {
            final Player player = Bukkit.getPlayer(genericPlayer.getUniqueId());
            if (player == null) return;

            player.getInventory().addItem(BasketballItemCollection.BASKETBALL);
        });
        setState(BasketballMatchState.GAME);
    }

    public void stop() {
        setState(BasketballMatchState.ENDING);

        shootedBasketballs.forEach((uuid, shootedBasketball) -> {
            shootedBasketball.deSpawn();
        });

        lobbyLocation.getNearbyEntitiesByType(Item.class, 50)
                .stream()
                .filter(item -> NBTMetadataUtil.hasString(item.getItemStack(), "accessor"))
                .forEach(Entity::remove);

        stopTasks();

        if (possibleWinnerTeam == null) {
            announcer.sendGlobalMessage(" \n&6&lThe game ended without a winner... let's try again!", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);
        } else {
            announcer.sendGlobalTitle(Title.title(
                    gameMiniMessage.deserialize("<green>Game ended!"),
                    gameMiniMessage.deserialize(
                            possibleWinnerTeam.getPlayers().stream().map(BasketballPlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")) + " <gray>won the match!"
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
            announcer.sendGlobalMessage(possibleWinnerTeam.getPlayers().stream().map(BasketballPlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")), true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 0.8F));

            basketballMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getBasketballMiniGame().getKaelaEvent(), new BasketballFireworkTask(this, possibleWinnerTeam), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getBasketballMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(basketballMatchProperties.getFireworkTaskId());
                basketballMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getBasketballMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (BasketballPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);

                BasketballItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
            });

            // Reset properties
            possibleWinnerTeam = null;
            basketballMatchProperties.resetTimer();
            shootedBasketballs.clear();
            basketballMatchProperties.setBallSpawnCooldown(10);

            setState(BasketballMatchState.WAITING);
        }, 120L);
    }

    public void checkWinner() {
        final List<BasketballTeam> basketballTeams = teamHelper.getTeamList().stream()
                .filter(BasketballTeam.class::isInstance)
                .map(BasketballTeam.class::cast)
                .toList();
        final int maxScore = basketballTeams.stream()
                .mapToInt(team -> team.getPlayers().stream()
                        .mapToInt(BasketballPlayer::getScore)
                        .sum())
                .max()
                .orElse(0);

        final List<BasketballTeam> topTeams = basketballTeams.stream()
                .filter(team -> team.getPlayers().stream()
                        .mapToInt(BasketballPlayer::getScore)
                        .sum() == maxScore)
                .toList();

        if (topTeams.size() == 1) {
            possibleWinnerTeam = topTeams.get(0);
        } else {
            possibleWinnerTeam = null;
        }
        stop();
    }


    public void spawnBalls() {
        final List<Location> spawnableSpots = basketballSpawnSpots.stream()
                .filter(location -> {
                    Collection<Item> spawnedBalls = location.getNearbyEntitiesByType(Item.class, 2);

                    return spawnedBalls.isEmpty();
                })
                .collect(Collectors.toList());

        if (spawnableSpots.isEmpty()) return;

        if (spawnableSpots.size() == 1) {
            final Location spot = spawnableSpots.get(0);
            final Item item = spot.getWorld().dropItem(spot, BasketballItemCollection.BASKETBALL);
            item.setGlowing(true);

            return;
        }

        Collections.shuffle(spawnableSpots);

        final Location firstSpot = spawnableSpots.get(0);
        final Location secondSpot = spawnableSpots.get(1);

        final Item firstItem = firstSpot.getWorld().dropItem(firstSpot, BasketballItemCollection.BASKETBALL);
        firstItem.setGlowing(true);

        final Item secondItem = secondSpot.getWorld().dropItem(secondSpot, BasketballItemCollection.BASKETBALL);
        secondItem.setGlowing(true);
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(basketballMatchProperties.getStartingTaskId());
        basketballMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(basketballMatchProperties.getPreLobbyTaskId());
        basketballMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(basketballMatchProperties.getArenaTickTaskId());
        basketballMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(basketballMatchProperties.getArenaTimeTaskId());
        basketballMatchProperties.setArenaTimeTaskId(0);
    }

    public int getTimeLeft() {
        return timeLimit - gameTime;
    }
}
