package com.covercorp.kaelaevent.minigame.games.zombie;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.announcer.AnnouncerImpl;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.ZombieArena;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.state.ZombieMatchState;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.ui.ZombieTeamChooseUI;
import com.covercorp.kaelaevent.minigame.games.zombie.command.ZombieChooseTeamCommand;
import com.covercorp.kaelaevent.minigame.games.zombie.config.ZombieConfigHelper;
import com.covercorp.kaelaevent.minigame.games.zombie.listener.ZombieAccessListener;
import com.covercorp.kaelaevent.minigame.games.zombie.listener.ZombieStatsListener;
import com.covercorp.kaelaevent.minigame.games.zombie.player.ZombiePlayer;
import com.covercorp.kaelaevent.minigame.games.zombie.team.ZombieTeam;
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
public final class ZombieMiniGame extends MiniGame {
    private ZombieConfigHelper configHelper;

    private PlayerHelper<ZombieMiniGame> playerHelper;
    private TeamHelper<ZombieMiniGame, ZombiePlayer> teamHelper;
    private Announcer<ZombieMiniGame> announcer;

    private ZombieArena arena;

    public ZombieMiniGame(final KaelaEvent kaelaEvent) {
        super(kaelaEvent, MiniGameType.ZOMBIE);

        configHelper = new ZombieConfigHelper(getGameConfiguration());

        playerHelper = new PlayerHelperImpl<>(this) {
            @Override
            public MiniGamePlayer<ZombieMiniGame> addPlayer(final Player player) {
                final ZombiePlayer zombiePlayer = (ZombiePlayer) players.put(player.getUniqueId(), new ZombiePlayer(miniGame, player.getUniqueId(), player.getName()));

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is now participating in the <green>Zombie Range<yellow> game!"
                ));

                return zombiePlayer;
            }

            @Override
            public MiniGamePlayer<ZombieMiniGame> removePlayer(final UUID uuid) {
                final Optional<MiniGamePlayer<ZombieMiniGame>> playerOptional = getPlayer(uuid);
                if (playerOptional.isEmpty()) return null;

                final MiniGamePlayer<ZombieMiniGame> player = playerOptional.get();
                if (player.getMiniGameTeam() != null) {
                    MiniGameTeam<ZombiePlayer> possibleTeam = (MiniGameTeam<ZombiePlayer>) player.getMiniGameTeam();

                    final ScoreboardManager scoreboardManager = miniGame.getKaelaEvent().getServer().getScoreboardManager();
                    final Team scoreboardTeam = scoreboardManager.getMainScoreboard().getTeam(possibleTeam.getIdentifier());
                    if (scoreboardTeam != null) scoreboardTeam.removeEntry(player.getName());

                    possibleTeam.removePlayer((ZombiePlayer) player);
                }

                final ZombiePlayer zombiePlayer = (ZombiePlayer) players.remove(uuid);

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is no longer participating in the <green>Zombie Range<yellow> game."
                ));

                return zombiePlayer;
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
                    final MiniGameTeam<ZombiePlayer> team = new ZombieTeam(identifier);
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
                    final Optional<MiniGameTeam<ZombiePlayer>> teamOptional = getTeam(teamIdentifier);
                    if (teamOptional.isEmpty()) return;

                    final MiniGameTeam<ZombiePlayer> team = teamOptional.get();

                    team.getPlayers().forEach(teamPlayer -> removePlayerFromTeam(teamPlayer, teamIdentifier));

                    final Team scoreboardTeam = scoreboard.getTeam(teamIdentifier);
                    if (scoreboardTeam != null) scoreboardTeam.unregister();

                    teams.remove(teamIdentifier);

                    getKaelaEvent().getLogger().info("Unregistered team " + teamIdentifier);
                });
            }
        };
        announcer = new AnnouncerImpl<>();

        arena = new ZombieArena(this);
    }

    @Override
    public void onGameLoad() {
        getKaelaEvent().getLogger().info("Loading game: Zombie");

        teamHelper.registerTeams();

        announcer = new AnnouncerImpl<>();

        getKaelaEvent().getServer().getPluginManager().registerEvents(new ZombieAccessListener(this), getKaelaEvent());
        getKaelaEvent().getServer().getPluginManager().registerEvents(new ZombieStatsListener(this), getKaelaEvent());

        getKaelaEvent().getServer().getPluginCommand("chooseteam").setExecutor(new ZombieChooseTeamCommand(this));
    }

    @Override
    public void onGameUnload() {
        getKaelaEvent().getLogger().info("Unloading game: Zombie");

        arena = null;

        teamHelper.unregisterTeams();
        teamHelper = null;

        playerHelper.clearPlayerList();
        playerHelper = null;

        configHelper = null;
    }

    public void openTeamInventory(final Player player) {
        if (arena.getState() != ZombieMatchState.WAITING) {
            player.sendMessage(getMiniMessage().deserialize("<red><bold>You can't choose team right now."));
            player.sendMessage(getMiniMessage().deserialize("<red><bold>Please wait until the actual game ends."));
            return;
        }

        final SmartInventory teamInventory = SmartInventory.builder()
                .id("TeamChooseUI")
                .manager(KaelaEvent.getKaelaEvent().getInventoryManager())
                .title("Choose a Team")
                .size(1, 9)
                .provider(new ZombieTeamChooseUI(this))
                .build();

        teamInventory.open(player);
    }
}
