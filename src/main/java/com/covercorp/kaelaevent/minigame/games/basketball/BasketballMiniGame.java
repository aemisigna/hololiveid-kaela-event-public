package com.covercorp.kaelaevent.minigame.games.basketball;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.announcer.AnnouncerImpl;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.BasketballArena;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.state.BasketballMatchState;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.ui.BasketballTeamChooseUI;
import com.covercorp.kaelaevent.minigame.games.basketball.command.BasketballChooseTeamCommand;
import com.covercorp.kaelaevent.minigame.games.basketball.config.BasketballConfigHelper;
import com.covercorp.kaelaevent.minigame.games.basketball.listener.BasketballAccessListener;
import com.covercorp.kaelaevent.minigame.games.basketball.listener.BasketballStatsListener;
import com.covercorp.kaelaevent.minigame.games.basketball.player.BasketballPlayer;
import com.covercorp.kaelaevent.minigame.games.basketball.team.BasketballTeam;
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
public final class BasketballMiniGame extends MiniGame {
    private BasketballConfigHelper configHelper;

    private PlayerHelper<BasketballMiniGame> playerHelper;
    private TeamHelper<BasketballMiniGame, BasketballPlayer> teamHelper;
    private Announcer<BasketballMiniGame> announcer;

    private BasketballArena arena;

    public BasketballMiniGame(final KaelaEvent kaelaEvent) {
        super(kaelaEvent, MiniGameType.BASKETBALL);

        configHelper = new BasketballConfigHelper(getGameConfiguration());

        playerHelper = new PlayerHelperImpl<>(this) {
            @Override
            public MiniGamePlayer<BasketballMiniGame> addPlayer(final Player player) {
                final BasketballPlayer basketballPlayer = (BasketballPlayer) players.put(player.getUniqueId(), new BasketballPlayer(miniGame, player.getUniqueId(), player.getName()));

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is now participating in the <green>Basketball <yellow>game!"
                ));

                return basketballPlayer;
            }

            @Override
            public MiniGamePlayer<BasketballMiniGame> removePlayer(final UUID uuid) {
                final Optional<MiniGamePlayer<BasketballMiniGame>> playerOptional = getPlayer(uuid);
                if (playerOptional.isEmpty()) return null;

                final MiniGamePlayer<BasketballMiniGame> player = playerOptional.get();
                if (player.getMiniGameTeam() != null) {
                    MiniGameTeam<BasketballPlayer> possibleTeam = (MiniGameTeam<BasketballPlayer>) player.getMiniGameTeam();

                    final ScoreboardManager scoreboardManager = miniGame.getKaelaEvent().getServer().getScoreboardManager();
                    final Team scoreboardTeam = scoreboardManager.getMainScoreboard().getTeam(possibleTeam.getIdentifier());
                    if (scoreboardTeam != null) scoreboardTeam.removeEntry(player.getName());

                    possibleTeam.removePlayer((BasketballPlayer) player);
                }

                final BasketballPlayer basketballPlayer = (BasketballPlayer) players.remove(uuid);

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is no longer participating in the <green>Basketball<yellow> game."
                ));

                return basketballPlayer;
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
                    final MiniGameTeam<BasketballPlayer> team = new BasketballTeam(identifier);
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
                    final Optional<MiniGameTeam<BasketballPlayer>> teamOptional = getTeam(teamIdentifier);
                    if (teamOptional.isEmpty()) return;

                    final MiniGameTeam<BasketballPlayer> team = teamOptional.get();

                    team.getPlayers().forEach(teamPlayer -> removePlayerFromTeam(teamPlayer, teamIdentifier));

                    final Team scoreboardTeam = scoreboard.getTeam(teamIdentifier);
                    if (scoreboardTeam != null) scoreboardTeam.unregister();

                    teams.remove(teamIdentifier);

                    getKaelaEvent().getLogger().info("Unregistered team " + teamIdentifier);
                });
            }
        };
        announcer = new AnnouncerImpl<>();

        arena = new BasketballArena(this);
    }

    @Override
    public void onGameLoad() {
        getKaelaEvent().getLogger().info("Loading game: Basketball");

        teamHelper.registerTeams();

        announcer = new AnnouncerImpl<>();

        getKaelaEvent().getServer().getPluginManager().registerEvents(new BasketballAccessListener(this), getKaelaEvent());
        getKaelaEvent().getServer().getPluginManager().registerEvents(new BasketballStatsListener(this), getKaelaEvent());

        getKaelaEvent().getServer().getPluginCommand("chooseteam").setExecutor(new BasketballChooseTeamCommand(this));
    }

    @Override
    public void onGameUnload() {
        getKaelaEvent().getLogger().info("Unloading game: Basketball");

        arena = null;

        teamHelper.unregisterTeams();
        teamHelper = null;

        playerHelper.clearPlayerList();
        playerHelper = null;

        configHelper = null;
    }

    public void openTeamInventory(final Player player) {
        if (arena.getState() != BasketballMatchState.WAITING) {
            player.sendMessage(getMiniMessage().deserialize("<red><bold>You can't choose team right now."));
            player.sendMessage(getMiniMessage().deserialize("<red><bold>Please wait until the actual game ends."));
            return;
        }

        final SmartInventory teamInventory = SmartInventory.builder()
                .id("TeamChooseUI")
                .manager(KaelaEvent.getKaelaEvent().getInventoryManager())
                .title("Choose a Team")
                .size(1, 9)
                .provider(new BasketballTeamChooseUI(this))
                .build();

        teamInventory.open(player);
    }
}
