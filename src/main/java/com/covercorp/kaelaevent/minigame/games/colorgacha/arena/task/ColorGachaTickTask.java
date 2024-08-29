package com.covercorp.kaelaevent.minigame.games.colorgacha.arena.task;

import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.ColorGachaArena;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.event.ColorGachaTickEvent;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.state.ColorGachaMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ColorGachaTickTask implements Runnable {
    private final ColorGachaArena arena;

    @Override
    public void run() {
        if (arena.getState() != ColorGachaMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new ColorGachaTickEvent(arena));
    }
}
