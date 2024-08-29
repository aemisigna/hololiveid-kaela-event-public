package com.covercorp.kaelaevent.minigame.games.platformrush.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.platformrush.PlatformRushMiniGame;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.listener.PlatformRushMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.listener.PlatformRushMatchListener;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.properties.PlatformRushMatchProperties;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.state.PlatformRushMatchState;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.task.PlatformRushFireworkTask;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.task.PlatformRushPreLobbyTask;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.task.PlatformRushTickTask;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.task.PlatformRushTimeTask;
import com.covercorp.kaelaevent.minigame.games.platformrush.inventory.PlatformRushItemCollection;
import com.covercorp.kaelaevent.minigame.games.platformrush.player.PlatformRushPlayer;
import com.covercorp.kaelaevent.minigame.games.platformrush.team.PlatformRushTeam;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
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

import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
public final class PlatformRushArena {
    private final PlatformRushMiniGame platformRushMiniGame;

    private final PlayerHelper<PlatformRushMiniGame> playerHelper;
    private final TeamHelper<PlatformRushMiniGame, PlatformRushPlayer> teamHelper;
    private final Announcer<PlatformRushMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final Location lobbyLocation;
    private final Location arenaSpawnLocation;

    private final PlatformRushMatchProperties platformRushMatchProperties;

    @Setter(AccessLevel.PUBLIC) private int gameTime;
    @Setter(AccessLevel.PUBLIC) private PlatformRushMatchState state;

    private final static String SCHEMATIC_NAME = "spleef_arena.schem";
    private final Map<String, File> mapSchematic;

    private PlatformRushTeam possibleWinnerTeam;

    public PlatformRushArena(final PlatformRushMiniGame platformRushMiniGame) {
        this.platformRushMiniGame = platformRushMiniGame;

        playerHelper = platformRushMiniGame.getPlayerHelper();
        teamHelper = platformRushMiniGame.getTeamHelper();
        announcer = platformRushMiniGame.getAnnouncer();

        gameMiniMessage = platformRushMiniGame.getMiniMessage();

        lobbyLocation = platformRushMiniGame.getConfigHelper().getLobbySpawn();
        arenaSpawnLocation = platformRushMiniGame.getConfigHelper().getArenaSpawn();

        platformRushMatchProperties = new PlatformRushMatchProperties(this);

        mapSchematic = Map.of(
                SCHEMATIC_NAME, new File(platformRushMiniGame.getKaelaEvent().getDataFolder() + File.separator + "schematics", SCHEMATIC_NAME)
        );

        Bukkit.getServer().getPluginManager().registerEvents(new PlatformRushMatchListener(this), getPlatformRushMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new PlatformRushMatchGameListener(this), getPlatformRushMiniGame().getKaelaEvent());

        resetMap();

        setState(PlatformRushMatchState.WAITING);
    }

    public void start() {
        // Cancel the match start item task
        Bukkit.getScheduler().cancelTask(getPlatformRushMatchProperties().getStartingTaskId());
        platformRushMatchProperties.setStartingTaskId(0);

        setGameTime(0);

        playerHelper.getPlayerList().forEach(participant -> {
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) return;

            final PlatformRushTeam team = (PlatformRushTeam) participant.getMiniGameTeam();
            if (team == null) return;

            player.teleport(getArenaSpawnLocation());
            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(false);
            player.setFlying(false);
        });

        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>PLATFORM RUSH"),
                gameMiniMessage.deserialize("<gray>Get ready!"),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(1000)
                )
        ));

        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);

        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lPlatform Rush", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lBreak the snow underneath your opponents to", true);
        announcer.sendGlobalMessage("&e&lmake them fall.", true);
        announcer.sendGlobalMessage("&e&lThe last talent alive wins the game!", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        // The task constructor has the setPreLobby(true) func
        platformRushMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(platformRushMiniGame.getKaelaEvent(), new PlatformRushPreLobbyTask(this), 0L, 20L).getTaskId()
        );
        setState(PlatformRushMatchState.ARENA_STARTING);
    }

    public void postStart() {
        // Cancel the previous task
        platformRushMatchProperties.setPreLobby(false);
        Bukkit.getScheduler().cancelTask(platformRushMatchProperties.getPreLobbyTaskId());

        platformRushMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getPlatformRushMiniGame().getKaelaEvent(), new PlatformRushTickTask(this), 0L, 1L).getTaskId()
        );
        platformRushMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getPlatformRushMiniGame().getKaelaEvent(), new PlatformRushTimeTask(this), 0L, 20L).getTaskId()
        );

        announcer.sendGlobalMessage("&eShovels enabled! Make them fall!", false);
        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>RUSH!"),
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(1000),
                        Duration.ofMillis(500)
                )
        ));
        announcer.sendGlobalSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0F, 2.0F);

        setState(PlatformRushMatchState.GAME);
    }

    public void stop() {
        setState(PlatformRushMatchState.ENDING);

        stopTasks();

        if (possibleWinnerTeam == null) {
            announcer.sendGlobalMessage(" \n&6&lThe game ended without a winner...", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);
        } else {
            announcer.sendGlobalTitle(Title.title(
                    gameMiniMessage.deserialize("<green>Game ended!"),
                    gameMiniMessage.deserialize(
                            possibleWinnerTeam.getPlayers().stream().map(PlatformRushPlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")) + " <gray>won the match!"
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
            announcer.sendGlobalMessage(possibleWinnerTeam.getPlayers().stream().map(PlatformRushPlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")), true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 0.8F));

            platformRushMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getPlatformRushMiniGame().getKaelaEvent(), new PlatformRushFireworkTask(this, possibleWinnerTeam), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getPlatformRushMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(platformRushMatchProperties.getFireworkTaskId());
                platformRushMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getPlatformRushMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (PlatformRushPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);

                PlatformRushItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            Bukkit.getOnlinePlayers().forEach(player -> player.teleport(lobbyLocation));

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
            });

            // Reset properties
            possibleWinnerTeam = null;
            platformRushMatchProperties.resetTimer();
            resetMap();

            setState(PlatformRushMatchState.WAITING);
        }, 120L);
    }

    public void resetMap() {
        final World world = new BukkitWorld(arenaSpawnLocation.getWorld());
        final BlockVector3 position = BlockVector3.at(lobbyLocation.getBlockX(), lobbyLocation.getBlockY(), lobbyLocation.getBlockZ());

        Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlatformRushMiniGame().getKaelaEvent(), () -> {
            final File schemFile = mapSchematic.get(SCHEMATIC_NAME);

            try (final EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                final ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(schemFile);
                if (clipboardFormat == null) {
                    return;
                }

                try (ClipboardReader reader = clipboardFormat.getReader(new FileInputStream(schemFile))) {
                    final Clipboard clipboard = reader.read();
                    final Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(position)
                            .ignoreAirBlocks(true)
                            .build();
                    Operations.complete(operation);

                } catch (Exception ignored) {}
            }
        });
    }

    public void checkWinner() {
        final List<PlatformRushTeam> aliveTeams = teamHelper.getTeamList().stream()
                .filter(PlatformRushTeam.class::isInstance)
                .map(PlatformRushTeam.class::cast)
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

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(platformRushMatchProperties.getStartingTaskId());
        platformRushMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(platformRushMatchProperties.getPreLobbyTaskId());
        platformRushMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(platformRushMatchProperties.getArenaTickTaskId());
        platformRushMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(platformRushMatchProperties.getArenaTimeTaskId());
        platformRushMatchProperties.setArenaTimeTaskId(0);
    }
}
