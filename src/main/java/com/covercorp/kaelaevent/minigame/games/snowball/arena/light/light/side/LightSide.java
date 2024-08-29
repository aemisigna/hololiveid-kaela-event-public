package com.covercorp.kaelaevent.minigame.games.snowball.arena.light.light.side;

import lombok.AccessLevel;
import lombok.Getter;

public enum LightSide {
    LEFT(0),
    RIGHT(1);

    @Getter(AccessLevel.PUBLIC) private final int teamIndex;

    LightSide(final int teamIndex) {
        this.teamIndex = teamIndex;
    }
}
