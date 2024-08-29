package com.covercorp.kaelaevent.minigame.games.tower.arena.task;

import com.covercorp.kaelaevent.minigame.games.tower.arena.TowerArena;
import com.covercorp.kaelaevent.minigame.games.tower.arena.state.TowerMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TowerTimeTask implements Runnable {
    private final TowerArena arena;

    public void run() {
        if (arena.getState() != TowerMatchState.GAME) return;

        arena.setGameTime(arena.getGameTime() + 1);
        arena.getAnnouncer().sendGlobalSound("kaela:tick", 0.1F, 1.7F);
    }
}