package com.covercorp.kaelaevent.minigame.games.colorgacha.arena.task;

import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.ColorGachaArena;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.state.ColorGachaMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ColorGachaTimeTask implements Runnable {
    private final ColorGachaArena arena;

    @Override
    public void run() {
        if (arena.getState() != ColorGachaMatchState.GAME) return;

        arena.setGameTime(arena.getGameTime() + 1); // Increase game time for this game

        if (arena.isPressingButton()) {
            // Warden sounds
            arena.getAnnouncer().sendGlobalSound(Sound.ENTITY_WARDEN_HEARTBEAT, 1.1F, 1.1F);
        }
    }
}
