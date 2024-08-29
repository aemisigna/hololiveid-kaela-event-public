package com.covercorp.kaelaevent.minigame.games.lava;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.announcer.AnnouncerImpl;
import com.covercorp.kaelaevent.minigame.games.lava.arena.LavaArena;
import com.covercorp.kaelaevent.minigame.games.lava.arena.state.LavaMatchState;
import com.covercorp.kaelaevent.minigame.games.lava.arena.ui.LavaTeamChooseUI;
import com.covercorp.kaelaevent.minigame.games.lava.command.LavaChooseTeamCommand;
import com.covercorp.kaelaevent.minigame.games.lava.config.LavaConfigHelper;
import com.covercorp.kaelaevent.minigame.games.lava.listener.LavaAccessListener;
import com.covercorp.kaelaevent.minigame.games.lava.listener.LavaStatsListener;
import com.covercorp.kaelaevent.minigame.games.lava.player.LavaPlayer;
import com.covercorp.kaelaevent.minigame.games.lava.team.LavaTeam;
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
public final class LavaMiniGame extends MiniGame {
    private LavaConfigHelper configHelper;

    private PlayerHelper<LavaMiniGame> playerHelper;
    private TeamHelper<LavaMiniGame, LavaPlayer> teamHelper;
    private Announcer<LavaMiniGame> announcer;

    private LavaArena arena;

    public LavaMiniGame(final KaelaEvent kaelaEvent) {
        super(kaelaEvent, MiniGameType.LAVA_ROOF);

        configHelper = new LavaConfigHelper(getGameConfiguration());

        playerHelper = new PlayerHelperImpl<>(this) {
            @Override
            public MiniGamePlayer<LavaMiniGame> addPlayer(final Player player) {
                final LavaPlayer lavaPlayer = (LavaPlayer) players.put(player.getUniqueId(), new LavaPlayer(miniGame, player.getUniqueId(), player.getName()));

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is now participating in the <green>Lava Roof<yellow> game!"
                ));

                return lavaPlayer;
            }

            @Override
            public MiniGamePlayer<LavaMiniGame> removePlayer(final UUID uuid) {
                final Optional<MiniGamePlayer<LavaMiniGame>> playerOptional = getPlayer(uuid);
                if (playerOptional.isEmpty()) return null;

                final MiniGamePlayer<LavaMiniGame> player = playerOptional.get();
                if (player.getMiniGameTeam() != null) {
                    MiniGameTeam<LavaPlayer> possibleTeam = (MiniGameTeam<LavaPlayer>) player.getMiniGameTeam();

                    final ScoreboardManager scoreboardManager = miniGame.getKaelaEvent().getServer().getScoreboardManager();
                    final Team scoreboardTeam = scoreboardManager.getMainScoreboard().getTeam(possibleTeam.getIdentifier());
                    if (scoreboardTeam != null) scoreboardTeam.removeEntry(player.getName());

                    possibleTeam.removePlayer((LavaPlayer) player);
                }

                final LavaPlayer lavaPlayer = (LavaPlayer) players.remove(uuid);

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is no longer participating in the <green>Lava Roof<yellow> game."
                ));

                return lavaPlayer;
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
                    final MiniGameTeam<LavaPlayer> team = new LavaTeam(identifier);
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
                    final Optional<MiniGameTeam<LavaPlayer>> teamOptional = getTeam(teamIdentifier);
                    if (teamOptional.isEmpty()) return;

                    final MiniGameTeam<LavaPlayer> team = teamOptional.get();

                    team.getPlayers().forEach(teamPlayer -> removePlayerFromTeam(teamPlayer, teamIdentifier));

                    final Team scoreboardTeam = scoreboard.getTeam(teamIdentifier);
                    if (scoreboardTeam != null) scoreboardTeam.unregister();

                    teams.remove(teamIdentifier);

                    getKaelaEvent().getLogger().info("Unregistered team " + teamIdentifier);
                });
            }
        };
        announcer = new AnnouncerImpl<>();

        arena = new LavaArena(this);
    }

    @Override
    public void onGameLoad() {
        getKaelaEvent().getLogger().info("Loading game: Lava Roof");

        teamHelper.registerTeams();

        announcer = new AnnouncerImpl<>();

        getKaelaEvent().getServer().getPluginManager().registerEvents(new LavaAccessListener(this), getKaelaEvent());
        getKaelaEvent().getServer().getPluginManager().registerEvents(new LavaStatsListener(this), getKaelaEvent());

        getKaelaEvent().getServer().getPluginCommand("chooseteam").setExecutor(new LavaChooseTeamCommand(this));
    }

    @Override
    public void onGameUnload() {
        getKaelaEvent().getLogger().info("Unloading game: Lava Roof");

        arena = null;

        teamHelper.unregisterTeams();
        teamHelper = null;

        playerHelper.clearPlayerList();
        playerHelper = null;

        configHelper = null;
    }

    public void openTeamInventory(final Player player) {
        if (arena.getState() != LavaMatchState.WAITING) {
            player.sendMessage(getMiniMessage().deserialize("<red><bold>You can't choose team right now."));
            player.sendMessage(getMiniMessage().deserialize("<red><bold>Please wait until the actual game ends."));
            return;
        }

        final SmartInventory teamInventory = SmartInventory.builder()
                .id("TeamChooseUI")
                .manager(KaelaEvent.getKaelaEvent().getInventoryManager())
                .title("Choose a Team")
                .size(1, 9)
                .provider(new LavaTeamChooseUI(this))
                .build();

        teamInventory.open(player);
    }
}
