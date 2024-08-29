package com.covercorp.kaelaevent.minigame.games.lava.arena.task;

import com.covercorp.kaelaevent.minigame.games.lava.arena.LavaArena;
import com.covercorp.kaelaevent.minigame.games.lava.arena.state.LavaMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class LavaShuffleBlocksTask implements Runnable {
    private final LavaArena arena;

    @Override
    public void run() {
        if (arena.getState() != LavaMatchState.GAME) return;

        if (!arena.isShuffling()) return;

        arena.changeSlots();
    }
}
