package com.covercorp.kaelaevent.minigame.games.glass;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.announcer.AnnouncerImpl;
import com.covercorp.kaelaevent.minigame.games.glass.arena.GlassArena;
import com.covercorp.kaelaevent.minigame.games.glass.arena.state.GlassMatchState;
import com.covercorp.kaelaevent.minigame.games.glass.arena.ui.GlassTeamChooseUI;
import com.covercorp.kaelaevent.minigame.games.glass.command.GlassChooseTeamCommand;
import com.covercorp.kaelaevent.minigame.games.glass.config.GlassConfigHelper;
import com.covercorp.kaelaevent.minigame.games.glass.listener.GlassAccessListener;
import com.covercorp.kaelaevent.minigame.games.glass.listener.GlassStatsListener;
import com.covercorp.kaelaevent.minigame.games.glass.player.GlassPlayer;
import com.covercorp.kaelaevent.minigame.games.glass.team.GlassTeam;
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
public final class GlassMiniGame extends MiniGame {
    private GlassConfigHelper configHelper;

    private PlayerHelper<GlassMiniGame> playerHelper;
    private TeamHelper<GlassMiniGame, GlassPlayer> teamHelper;
    private Announcer<GlassMiniGame> announcer;

    private GlassArena arena;
    
    public GlassMiniGame(KaelaEvent kaelaEvent) {
        super(kaelaEvent, MiniGameType.GLASS_BRIDGE);
        
        this.configHelper = new GlassConfigHelper(getGameConfiguration());

        playerHelper = new PlayerHelperImpl<>(this) {
            @Override
            public MiniGamePlayer<GlassMiniGame> addPlayer(final Player player) {
                final GlassPlayer glassPlayer = (GlassPlayer) players.put(player.getUniqueId(), new GlassPlayer(miniGame, player.getUniqueId(), player.getName()));

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is now participating in the <green>Glass Bridge Crossing<yellow> game!"
                ));

                return glassPlayer;
            }

            @Override
            public MiniGamePlayer<GlassMiniGame> removePlayer(final UUID uuid) {
                final Optional<MiniGamePlayer<GlassMiniGame>> playerOptional = getPlayer(uuid);
                if (playerOptional.isEmpty()) return null;

                final MiniGamePlayer<GlassMiniGame> player = playerOptional.get();
                if (player.getMiniGameTeam() != null) {
                    MiniGameTeam<GlassPlayer> possibleTeam = (MiniGameTeam<GlassPlayer>) player.getMiniGameTeam();

                    final ScoreboardManager scoreboardManager = miniGame.getKaelaEvent().getServer().getScoreboardManager();
                    final Team scoreboardTeam = scoreboardManager.getMainScoreboard().getTeam(possibleTeam.getIdentifier());
                    if (scoreboardTeam != null) scoreboardTeam.removeEntry(player.getName());

                    possibleTeam.removePlayer((GlassPlayer) player);
                }

                final GlassPlayer glassPlayer = (GlassPlayer) players.remove(uuid);

                miniGame.getAnnouncer().sendGlobalComponent(miniGame.getMiniMessage().deserialize(
                        "<aqua>" + player.getName() + " <yellow>is no longer participating in the <green>Glass Bridge Crossing<yellow> game."
                ));

                return glassPlayer;
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
                    final MiniGameTeam<GlassPlayer> team = new GlassTeam(identifier);
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
                    final Optional<MiniGameTeam<GlassPlayer>> teamOptional = getTeam(teamIdentifier);
                    if (teamOptional.isEmpty()) return;

                    final MiniGameTeam<GlassPlayer> team = teamOptional.get();

                    team.getPlayers().forEach(teamPlayer -> removePlayerFromTeam(teamPlayer, teamIdentifier));

                    final Team scoreboardTeam = scoreboard.getTeam(teamIdentifier);
                    if (scoreboardTeam != null) scoreboardTeam.unregister();

                    teams.remove(teamIdentifier);

                    getKaelaEvent().getLogger().info("Unregistered team " + teamIdentifier);
                });
            }
        };
        announcer = new AnnouncerImpl<>();

        arena = new GlassArena(this);
    }

    public void onGameLoad() {
        getKaelaEvent().getLogger().info("Loading game: Glass Bridge Crossing");

        teamHelper.registerTeams();

        announcer = new AnnouncerImpl<>();

        getKaelaEvent().getServer().getPluginManager().registerEvents(new GlassAccessListener(this), getKaelaEvent());
        getKaelaEvent().getServer().getPluginManager().registerEvents(new GlassStatsListener(this), getKaelaEvent());

        getKaelaEvent().getServer().getPluginCommand("chooseteam").setExecutor(new GlassChooseTeamCommand(this));
    }

    public void onGameUnload() {
        getKaelaEvent().getLogger().info("Unloading game: Glass Bridge Crossing");
        arena = null;

        teamHelper.unregisterTeams();
        teamHelper = null;

        playerHelper.clearPlayerList();
        playerHelper = null;

        configHelper = null;
    }

    public void openTeamInventory(Player player) {
        if (this.arena.getState() != GlassMatchState.WAITING) {
            player.sendMessage(getMiniMessage().deserialize("<red><bold>You can't choose team right now."));
            player.sendMessage(getMiniMessage().deserialize("<red><bold>Please wait until the actual game ends."));
            return;
        }

        SmartInventory teamInventory = SmartInventory.builder().id("TeamChooseUI").manager(KaelaEvent.getKaelaEvent().getInventoryManager()).title("Choose a Team").size(1, 9).provider(new GlassTeamChooseUI(this)).build();
        teamInventory.open(player);
    }
}