package com.covercorp.kaelaevent.minigame.games.trident;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.announcer.AnnouncerImpl;
import com.covercorp.kaelaevent.minigame.games.trident.arena.TridentArena;
import com.covercorp.kaelaevent.minigame.games.trident.arena.state.TridentMatchState;
import com.covercorp.kaelaevent.minigame.games.trident.arena.ui.TridentTeamChooseUI;
import com.covercorp.kaelaevent.minigame.games.trident.command.TridentChooseTeamCommand;
import com.covercorp.kaelaevent.minigame.games.trident.config.TridentConfigHelper;
import com.covercorp.kaelaevent.minigame.games.trident.listener.TridentAccessListener;
import com.covercorp.kaelaevent.minigame.games.trident.listener.TridentStatsListener;
import com.covercorp.kaelaevent.minigame.games.trident.player.TridentPlayer;
import com.covercorp.kaelaevent.minigame.games.trident.team.TridentTeam;
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
public final class TridentMiniGame extends MiniGame {
    private TridentConfigHelper configHelper;

    private PlayerHelper<TridentMiniGame> playerHelper;
    private TeamHelper<TridentMiniGame, TridentPlayer> teamHelper;
    private Announcer<TridentMiniGame> announcer;

    private TridentArena arena;

    public TridentMiniGame(final KaelaEvent kaelaEvent) {
        super(kaelaEvent, MiniGameType.TRIDENT_RACE);

        configHelper = new TridentConfigHelper(getGameConfiguration());

        playerHelper = new PlayerHelperImpl<>(this) {
            @Override
            public MiniGamePlayer<TridentMiniGame> addPlayer(final Player player) {
                final TridentPlayer tridentPlayer = (TridentPlayer) players.put(player.getUniqueId(), new TridentPlayer(miniGame, player.getUniqueId(), player.getName()));

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is now participating in the <green>Trident Race<yellow> game!"
                ));

                return tridentPlayer;
            }

            @Override
            public MiniGamePlayer<TridentMiniGame> removePlayer(final UUID uuid) {
                final Optional<MiniGamePlayer<TridentMiniGame>> playerOptional = getPlayer(uuid);
                if (playerOptional.isEmpty()) return null;

                final MiniGamePlayer<TridentMiniGame> player = playerOptional.get();
                if (player.getMiniGameTeam() != null) {
                    MiniGameTeam<TridentPlayer> possibleTeam = (MiniGameTeam<TridentPlayer>) player.getMiniGameTeam();

                    final ScoreboardManager scoreboardManager = miniGame.getKaelaEvent().getServer().getScoreboardManager();
                    final Team scoreboardTeam = scoreboardManager.getMainScoreboard().getTeam(possibleTeam.getIdentifier());
                    if (scoreboardTeam != null) scoreboardTeam.removeEntry(player.getName());

                    possibleTeam.removePlayer((TridentPlayer) player);
                }

                final TridentPlayer tridentPlayer = (TridentPlayer) players.remove(uuid);

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is no longer participating in the <green>Trident Race<yellow> game."
                ));

                return tridentPlayer;
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
                    final MiniGameTeam<TridentPlayer> team = new TridentTeam(identifier);
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
                    final Optional<MiniGameTeam<TridentPlayer>> teamOptional = getTeam(teamIdentifier);
                    if (teamOptional.isEmpty()) return;

                    final MiniGameTeam<TridentPlayer> team = teamOptional.get();

                    team.getPlayers().forEach(teamPlayer -> removePlayerFromTeam(teamPlayer, teamIdentifier));

                    final Team scoreboardTeam = scoreboard.getTeam(teamIdentifier);
                    if (scoreboardTeam != null) scoreboardTeam.unregister();

                    teams.remove(teamIdentifier);

                    getKaelaEvent().getLogger().info("Unregistered team " + teamIdentifier);
                });
            }
        };
        announcer = new AnnouncerImpl<>();

        arena = new TridentArena(this);
    }

    @Override
    public void onGameLoad() {
        getKaelaEvent().getLogger().info("Loading game: Trident");

        teamHelper.registerTeams();

        announcer = new AnnouncerImpl<>();

        getKaelaEvent().getServer().getPluginManager().registerEvents(new TridentAccessListener(this), getKaelaEvent());
        getKaelaEvent().getServer().getPluginManager().registerEvents(new TridentStatsListener(this), getKaelaEvent());

        getKaelaEvent().getServer().getPluginCommand("chooseteam").setExecutor(new TridentChooseTeamCommand(this));
    }

    @Override
    public void onGameUnload() {
        getKaelaEvent().getLogger().info("Unloading game: Trident");

        arena = null;

        teamHelper.unregisterTeams();
        teamHelper = null;

        playerHelper.clearPlayerList();
        playerHelper = null;

        configHelper = null;
    }

    public void openTeamInventory(final Player player) {
        if (arena.getState() != TridentMatchState.WAITING) {
            player.sendMessage(getMiniMessage().deserialize("<red><bold>You can't choose team right now."));
            player.sendMessage(getMiniMessage().deserialize("<red><bold>Please wait until the actual game ends."));
            return;
        }

        final SmartInventory teamInventory = SmartInventory.builder()
                .id("TeamChooseUI")
                .manager(KaelaEvent.getKaelaEvent().getInventoryManager())
                .title("Choose a Team")
                .size(1, 9)
                .provider(new TridentTeamChooseUI(this))
                .build();

        teamInventory.open(player);
    }
}
