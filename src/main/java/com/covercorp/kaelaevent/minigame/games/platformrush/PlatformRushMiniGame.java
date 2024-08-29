package com.covercorp.kaelaevent.minigame.games.platformrush;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.announcer.AnnouncerImpl;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.PlatformRushArena;
import com.covercorp.kaelaevent.minigame.games.platformrush.listener.PlatformRushAccessListener;
import com.covercorp.kaelaevent.minigame.games.platformrush.listener.PlatformRushStatsListener;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.state.PlatformRushMatchState;
import com.covercorp.kaelaevent.minigame.games.platformrush.command.PlatformRushChooseTeamCommand;
import com.covercorp.kaelaevent.minigame.games.platformrush.config.PlatformRushConfigHelper;
import com.covercorp.kaelaevent.minigame.games.platformrush.player.PlatformRushPlayer;
import com.covercorp.kaelaevent.minigame.games.platformrush.team.PlatformRushTeam;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.ui.PlatformRushTeamChooseUI;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.player.PlayerHelperImpl;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelperImpl;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import com.covercorp.kaelaevent.minigame.type.MiniGameType;

import com.covercorp.kaelaevent.util.PlayerUtils;
import com.google.common.collect.ImmutableList;
import fr.minuskube.inv.SmartInventory;

import lombok.AccessLevel;
import lombok.Getter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
public final class PlatformRushMiniGame extends MiniGame {
    private PlatformRushConfigHelper configHelper;

    private PlayerHelper<PlatformRushMiniGame> playerHelper;
    private TeamHelper<PlatformRushMiniGame, PlatformRushPlayer> teamHelper;
    private Announcer<PlatformRushMiniGame> announcer;

    private PlatformRushArena arena;

    public PlatformRushMiniGame(KaelaEvent kaelaEvent) {
        super(kaelaEvent, MiniGameType.PLATFORM_RUSH);

        configHelper = new PlatformRushConfigHelper(getGameConfiguration());

        playerHelper = new PlayerHelperImpl<>(this) {
            @Override
            public MiniGamePlayer<PlatformRushMiniGame> addPlayer(final Player player) {
                final PlatformRushPlayer platformRushPlayer = (PlatformRushPlayer) players.put(player.getUniqueId(), new PlatformRushPlayer(miniGame, player.getUniqueId(), player.getName()));

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is now participating in the <green>Platform Rush <yellow>game!"
                ));

                return platformRushPlayer;
            }

            @Override
            public MiniGamePlayer<PlatformRushMiniGame> removePlayer(final UUID uuid) {
                Optional<MiniGamePlayer<PlatformRushMiniGame>> playerOptional = getPlayer(uuid);
                if (playerOptional.isEmpty()) return null;

                MiniGamePlayer<PlatformRushMiniGame> player = playerOptional.get();
                if (player.getMiniGameTeam() != null) {
                    MiniGameTeam<PlatformRushPlayer> possibleTeam = (MiniGameTeam<PlatformRushPlayer>) player.getMiniGameTeam();

                    final ScoreboardManager scoreboardManager = miniGame.getKaelaEvent().getServer().getScoreboardManager();
                    final Team scoreboardTeam = scoreboardManager.getMainScoreboard().getTeam(possibleTeam.getIdentifier());
                    if (scoreboardTeam != null) scoreboardTeam.removeEntry(player.getName());

                    possibleTeam.removePlayer((PlatformRushPlayer) player);
                }

                PlatformRushPlayer platformRushPlayer = (PlatformRushPlayer) players.remove(uuid);

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is no longer participating in the <green>Platform Rush<yellow> game."
                ));

                return platformRushPlayer;
            }
        };

        // Clear scoreboard
        final ScoreboardManager scoreboardManager = getKaelaEvent().getServer().getScoreboardManager();
        final Scoreboard scoreboard = scoreboardManager.getMainScoreboard();

        teamHelper = new TeamHelperImpl<>(this) {
            @Override
            public void registerTeams() {

                // Delete old teams
                scoreboard.getTeams().forEach(Team::unregister);

                final List<String> colors = ImmutableList.of(
                        "#6bceff",
                        "#77ff6b",
                        "#ff6bc4",
                        "#ffa66b",
                        "#776bff",
                        "#ff6bf5",
                        "#ff956b"
                );

                for (int i = 1; i <= configHelper.getMaxTeams(); i++) {
                    final String identifier = "team_" + i;
                    final MiniGameTeam<PlatformRushPlayer> team = new PlatformRushTeam(identifier);
                    final Component prefix = miniGame.getMiniMessage().deserialize("<" + colors.get(new Random().nextInt(colors.size())) + ">" + "[Team #" + i + "] ");

                    team.setPrefix(prefix);

                    final Component betterPrefix = getMiniMessage().deserialize(PlayerUtils.getBetterPrefix(i));
                    team.setBetterPrefix(betterPrefix);

                    final Team scoreboardTeam = scoreboard.registerNewTeam(identifier);
                    scoreboardTeam.prefix(betterPrefix);

                    teams.put(team.getIdentifier(), team);
                }
            }

            @Override
            public void unregisterTeams() {
                getTeamList().forEach(registeredTeam -> {
                    final String teamIdentifier = registeredTeam.getIdentifier();
                    final Optional<MiniGameTeam<PlatformRushPlayer>> teamOptional = getTeam(teamIdentifier);
                    if (teamOptional.isEmpty()) return;

                    final MiniGameTeam<PlatformRushPlayer> team = teamOptional.get();

                    team.getPlayers().forEach(teamPlayer -> {
                        removePlayerFromTeam(teamPlayer, teamIdentifier);
                    });

                    final Team scoreboardTeam = scoreboard.getTeam(teamIdentifier);
                    if (scoreboardTeam != null) scoreboardTeam.unregister();

                    teams.remove(teamIdentifier);

                    getKaelaEvent().getLogger().info("Unregistered team " + teamIdentifier);
                });
            }
        };
        announcer = new AnnouncerImpl<>();

        arena = new PlatformRushArena(this);
    }

    @Override
    public void onGameLoad() {
        getKaelaEvent().getLogger().info("Loading game: Platform Rush");

        teamHelper.registerTeams();

        announcer = new AnnouncerImpl<>();

        getKaelaEvent().getServer().getPluginManager().registerEvents(new PlatformRushAccessListener(this), getKaelaEvent());
        getKaelaEvent().getServer().getPluginManager().registerEvents(new PlatformRushStatsListener(this), getKaelaEvent());

        getKaelaEvent().getServer().getPluginCommand("chooseteam").setExecutor(new PlatformRushChooseTeamCommand(this));
    }

    @Override
    public void onGameUnload() {
        getKaelaEvent().getLogger().info("Unloading game: Platform Rush");

        arena = null;

        teamHelper.unregisterTeams();
        teamHelper = null;

        playerHelper.clearPlayerList();
        playerHelper = null;

        configHelper = null;
    }

    public void openTeamInventory(final Player player) {
        if (arena.getState() != PlatformRushMatchState.WAITING) {
            player.sendMessage(getMiniMessage().deserialize("<red><bold>You can't choose team right now."));
            player.sendMessage(getMiniMessage().deserialize("<red><bold>Please wait until the actual game ends."));
            return;
        }

        final SmartInventory teamInventory = SmartInventory.builder()
                .id("TeamChooseUI")
                .manager(KaelaEvent.getKaelaEvent().getInventoryManager())
                .title("Choose a Team")
                .size(1, 9)
                .provider(new PlatformRushTeamChooseUI(this))
                .build();

        teamInventory.open(player);
    }
}
