package com.covercorp.kaelaevent.minigame.games.reflex.team;

import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.ReflexSpot;
import com.covercorp.kaelaevent.minigame.games.reflex.player.ReflexPlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class ReflexTeam extends MiniGameTeam<ReflexPlayer> {
    private ReflexSpot reflexSpot;
    private int score;

    public ReflexTeam(final String identifier) {
        super(identifier);
    }

    public ReflexPlayer getFirstPlayer() {
        return getPlayers().stream().findFirst().orElse(null);
    }
}