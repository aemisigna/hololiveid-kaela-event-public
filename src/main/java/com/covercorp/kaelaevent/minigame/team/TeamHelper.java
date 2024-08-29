package com.covercorp.kaelaevent.minigame.team;

import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import com.google.common.collect.ImmutableList;

import java.util.Optional;

public interface TeamHelper<T extends MiniGame, C extends MiniGamePlayer<T>> {
    void registerTeams();
    void unregisterTeams();

    MiniGameTeam<C> addPlayerToTeam(C player, String teamIdentifier);
    MiniGameTeam<C> removePlayerFromTeam(C player, String teamIdentifier);

    Optional<MiniGameTeam<C>> getTeam(final String teamIdentifier);

    ImmutableList<MiniGameTeam<C>> getTeamList();
}