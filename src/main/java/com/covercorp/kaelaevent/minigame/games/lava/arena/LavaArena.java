package com.covercorp.kaelaevent.minigame.games.lava.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.lava.LavaMiniGame;
import com.covercorp.kaelaevent.minigame.games.lava.arena.event.LavaRoundStartEvent;
import com.covercorp.kaelaevent.minigame.games.lava.arena.listener.LavaMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.lava.arena.listener.LavaMatchListener;
import com.covercorp.kaelaevent.minigame.games.lava.arena.properties.LavaMatchProperties;
import com.covercorp.kaelaevent.minigame.games.lava.arena.slot.SlotHelper;
import com.covercorp.kaelaevent.minigame.games.lava.arena.slot.slot.LavaSlot;
import com.covercorp.kaelaevent.minigame.games.lava.arena.slot.slot.state.SlotStatus;
import com.covercorp.kaelaevent.minigame.games.lava.arena.state.LavaMatchState;
import com.covercorp.kaelaevent.minigame.games.lava.arena.task.*;
import com.covercorp.kaelaevent.minigame.games.lava.inventory.LavaItemCollection;
import com.covercorp.kaelaevent.minigame.games.lava.player.LavaPlayer;
import com.covercorp.kaelaevent.minigame.games.lava.team.LavaTeam;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.BlockUtils;
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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import static java.util.Map.entry;

@Getter(AccessLevel.PUBLIC)
public final class LavaArena {
    private final LavaMiniGame lavaMiniGame;

    private final PlayerHelper<LavaMiniGame> playerHelper;
    private final TeamHelper<LavaMiniGame, LavaPlayer> teamHelper;
    private final Announcer<LavaMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final Location lobbyLocation;
    private final Location arenaSpawnLocation;

    private final SlotHelper slotHelper;

    private final LavaMatchProperties lavaMatchProperties;

    @Setter(AccessLevel.PUBLIC) private int gameTime;
    @Setter(AccessLevel.PUBLIC) private LavaMatchState state;

    private LavaTeam possibleWinnerTeam;

    @Setter(AccessLevel.PUBLIC) private boolean isShuffling;
    @Setter(AccessLevel.PUBLIC) private boolean isChecking;
    private final Map<Integer, Integer> roundTimes = Map.ofEntries(
            entry(1, 10),
            entry(2, 9),
            entry(3, 9),
            entry(4, 8),
            entry(5, 7),
            entry(6, 7),
            entry(7, 6),
            entry(8, 5),
            entry(9, 4),
            entry(10, 4),
            entry(11, 3),
            entry(12, 3),
            entry(13, 3),
            entry(14, 2)
    );

    private final Map<Integer, Integer> roundSlots = Map.ofEntries(
            entry(1, 5),
            entry(2, 5),
            entry(3, 4),
            entry(4, 4),
            entry(5, 4),
            entry(6, 3),
            entry(7, 3),
            entry(8, 3),
            entry(9, 2),
            entry(10, 2),
            entry(11, 2),
            entry(12, 1)
    );

    public LavaArena(final LavaMiniGame lavaMiniGame) {
        this.lavaMiniGame = lavaMiniGame;

        playerHelper = lavaMiniGame.getPlayerHelper();
        teamHelper = lavaMiniGame.getTeamHelper();
        announcer = lavaMiniGame.getAnnouncer();

        gameMiniMessage = lavaMiniGame.getMiniMessage();

        slotHelper = new SlotHelper(this);

        lobbyLocation = lavaMiniGame.getConfigHelper().getLobbySpawn();
        arenaSpawnLocation = lavaMiniGame.getConfigHelper().getArenaSpawn();

        lavaMatchProperties = new LavaMatchProperties(this);

        isShuffling = false;
        isChecking = false;

        Bukkit.getServer().getPluginManager().registerEvents(new LavaMatchListener(this), getLavaMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new LavaMatchGameListener(this), getLavaMiniGame().getKaelaEvent());

        setState(LavaMatchState.WAITING);
    }

    public void start() {
        // Cancel the match start item task
        Bukkit.getScheduler().cancelTask(getLavaMatchProperties().getStartingTaskId());
        lavaMatchProperties.setStartingTaskId(0);

        setGameTime(0);
        lavaMatchProperties.setShuffleTime(5);
        lavaMatchProperties.setBlockCooldown(10);

        getLavaMiniGame().getConfigHelper().getSlots().forEach(slotIdentifier -> {
            final Location center = getLavaMiniGame().getConfigHelper().getSlot(slotIdentifier);
            final List<Block> blockList = BlockUtils.getAdjacentBlocks(center);
            final List<Location> locations = blockList.stream().map(Block::getLocation).toList();

            slotHelper.addSlot(new LavaSlot(slotIdentifier, center, locations));
        });

        slotHelper.getSlots().forEach(slot -> slot.setStatus(SlotStatus.SAFE));

        playerHelper.getPlayerList().forEach(participant -> {
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) return;

            final LavaTeam team = (LavaTeam) participant.getMiniGameTeam();
            if (team == null) return;

            player.teleport(getArenaSpawnLocation());
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setFlying(false);
        });

        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>LAVA ROOF"),
                gameMiniMessage.deserialize("<gray>Get ready!"),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(1000)
                )
        ));

        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);

        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lThe Roof is Lava", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lMove to the green slots before the time", true);
        announcer.sendGlobalMessage("&e&lruns out to avoid being pushed up to the lava.", true);
        announcer.sendGlobalMessage("&e&lThe more rounds pass, the faster the slots change.", true);
        announcer.sendGlobalMessage("&e&lThe last talent alive wins!", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        // The task constructor has the setPreLobby(true) fnc
        lavaMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(lavaMiniGame.getKaelaEvent(), new LavaPreLobbyTask(this), 0L, 20L).getTaskId()
        );
        setState(LavaMatchState.ARENA_STARTING);
    }

    public void postStart() {
        // Cancel the previous task
        lavaMatchProperties.setPreLobby(false);
        Bukkit.getScheduler().cancelTask(lavaMatchProperties.getPreLobbyTaskId());
        Bukkit.getScheduler().cancelTask(lavaMatchProperties.getArenaShuffleTaskId());

        lavaMatchProperties.setArenaShuffleTaskId(
                Bukkit.getScheduler().runTaskTimer(getLavaMiniGame().getKaelaEvent(), new LavaShuffleBlocksTask(this), 0L, 5L).getTaskId()
        );

        lavaMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getLavaMiniGame().getKaelaEvent(), new LavaTickTask(this), 0L, 1L).getTaskId()
        );
        lavaMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getLavaMiniGame().getKaelaEvent(), new LavaTimeTask(this), 0L, 20L).getTaskId()
        );

        announcer.sendGlobalMessage("&eHere it comes...", false);
        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>START!"),
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(1000),
                        Duration.ofMillis(0)
                )
        ));

        announcer.sendGlobalSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0F, 2.0F);

        setState(LavaMatchState.GAME);

        getLavaMatchProperties().setRound(0);
        Bukkit.getPluginManager().callEvent(new LavaRoundStartEvent(this));
    }

    public void stop() {
        setState(LavaMatchState.ENDING);

        stopTasks();

        if (possibleWinnerTeam == null) {
            announcer.sendGlobalMessage(" \n&6&lThe game ended without a winner... let's try again!", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);
        } else {
            announcer.sendGlobalTitle(Title.title(
                    gameMiniMessage.deserialize("<green>Game ended!"),
                    gameMiniMessage.deserialize(
                            possibleWinnerTeam.getPlayers().stream().map(LavaPlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")) + " <gray>won the match!"
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
            announcer.sendGlobalMessage(possibleWinnerTeam.getPlayers().stream().map(LavaPlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")), true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 0.8F));

            lavaMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getLavaMiniGame().getKaelaEvent(), new LavaFireworkTask(this, possibleWinnerTeam), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getLavaMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(lavaMatchProperties.getFireworkTaskId());
                lavaMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getLavaMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (LavaPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);

                LavaItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
            });

            // Reset properties
            possibleWinnerTeam = null;
            lavaMatchProperties.resetTimer();
            resetSlots();

            setState(LavaMatchState.WAITING);
        }, 120L);
    }

    public void checkWinner() {
        final List<LavaTeam> aliveTeams = teamHelper.getTeamList().stream()
                .filter(LavaTeam.class::isInstance)
                .map(LavaTeam.class::cast)
                .filter(team -> team.getPlayers().stream().anyMatch(player -> !player.isDead()))
                .toList();

        if (aliveTeams.size() == 1) {
            possibleWinnerTeam = aliveTeams.get(0);
            stop();
        } else if (aliveTeams.isEmpty()) {
            possibleWinnerTeam = null;
            stop();
        }
    }

    public void changeSlots() {
        final List<LavaSlot> slots = slotHelper.getSlots();

        // reset
        resetSlots();

        Collections.shuffle(slots);

        // Select all slots except for X, and change their status
        for (int i = 0; i < slots.size() - getRoundSlotSize(); i++) {
            slots.get(i).setStatus(SlotStatus.WARNING);
        }

        announcer.sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.3F, 1.3F);
    }

    public void resetSlots() {
        final List<LavaSlot> slots = slotHelper.getSlots();

        // reset
        slots.forEach(slot -> slot.setStatus(SlotStatus.SAFE));
    }

    public int getRoundRunTime() {
        if (!roundTimes.containsKey(getLavaMatchProperties().getRound())) return 2; // return default if round is not present

        return roundTimes.get(getLavaMatchProperties().getRound());
    }

    public int getRoundSlotSize() {
        if (!roundSlots.containsKey(getLavaMatchProperties().getRound())) return 1; // return default if round is not present

        return roundSlots.get(getLavaMatchProperties().getRound());
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(lavaMatchProperties.getStartingTaskId());
        lavaMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(lavaMatchProperties.getPreLobbyTaskId());
        lavaMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(lavaMatchProperties.getArenaTickTaskId());
        lavaMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(lavaMatchProperties.getArenaTimeTaskId());
        lavaMatchProperties.setArenaTimeTaskId(0);
        Bukkit.getScheduler().cancelTask(lavaMatchProperties.getArenaShuffleTaskId());
        lavaMatchProperties.setArenaShuffleTaskId(0);
    }
}
