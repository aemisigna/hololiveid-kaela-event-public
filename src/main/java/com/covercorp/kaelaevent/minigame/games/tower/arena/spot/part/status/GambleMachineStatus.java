package com.covercorp.kaelaevent.minigame.games.tower.arena.spot.part.status;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public enum GambleMachineStatus {
    NO_ROLL("No Roll", -1),
    ROLL("Rolling...", -1),
    REDSTONE("REDSTONE", 1),
    GOLD("GOLD", 2),
    EMERALD("EMERALD", 3),
    DIAMOND("DIAMOND", 4),
    NETHERITE("NETHERITE", 5);

    private final String name;
    private final int level;
}
