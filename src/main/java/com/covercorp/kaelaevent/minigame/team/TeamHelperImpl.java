package com.covercorp.kaelaevent.minigame.team;

import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import com.google.common.collect.ImmutableList;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class TeamHelperImpl<T extends MiniGame, C extends MiniGamePlayer<T>> implements TeamHelper<T, C> {
    protected final T miniGame;

    protected final Map<String, MiniGameTeam<C>> teams;

    public TeamHelperImpl(final T miniGame) {
        this.miniGame = miniGame;

        teams = new LinkedHashMap<>();
    }

    @Override
    public abstract void registerTeams();

    @Override
    public abstract void unregisterTeams();

    @Override
    public MiniGameTeam<C> addPlayerToTeam(C player, String teamIdentifier) {
        if (player.getMiniGameTeam() != null) {
            removePlayerFromTeam(player, teamIdentifier);
        }

        final Optional<MiniGameTeam<C>> teamOptional = getTeam(teamIdentifier);
        if (teamOptional.isEmpty()) return null;

        final MiniGameTeam<C> team = teamOptional.get();

        team.addPlayer(player);
        player.setMiniGameTeam(team);

        final ScoreboardManager scoreboardManager = miniGame.getKaelaEvent().getServer().getScoreboardManager();
        final Team scoreboardTeam = scoreboardManager.getMainScoreboard().getTeam(teamIdentifier);
        if (scoreboardTeam != null) scoreboardTeam.addEntry(player.getName());

        return team;
    }

    @Override
    public MiniGameTeam<C> removePlayerFromTeam(C player, String teamIdentifier) {
        final Optional<MiniGameTeam<C>> teamOptional = getTeam(teamIdentifier);
        if (teamOptional.isEmpty()) return null;

        final MiniGameTeam<C> team = teamOptional.get();

        team.removePlayer(player);
        player.setMiniGameTeam(null);

        final ScoreboardManager scoreboardManager = miniGame.getKaelaEvent().getServer().getScoreboardManager();
        final Team scoreboardTeam = scoreboardManager.getMainScoreboard().getTeam(teamIdentifier);
        if (scoreboardTeam != null) scoreboardTeam.removeEntry(player.getName());

        return team;
    }

    @Override
    public Optional<MiniGameTeam<C>> getTeam(String teamIdentifier) {
        return Optional.ofNullable(teams.get(teamIdentifier));
    }

    @Override
    public ImmutableList<MiniGameTeam<C>> getTeamList() {
        return ImmutableList.copyOf(teams.values());
    }
}
