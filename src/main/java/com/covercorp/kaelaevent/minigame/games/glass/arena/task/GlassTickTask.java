package com.covercorp.kaelaevent.minigame.games.glass.arena.task;

import com.covercorp.kaelaevent.minigame.games.glass.arena.GlassArena;
import com.covercorp.kaelaevent.minigame.games.glass.arena.event.GlassTickEvent;
import com.covercorp.kaelaevent.minigame.games.glass.arena.state.GlassMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class GlassTickTask implements Runnable {
    private final GlassArena arena;

    @Override
    public void run() {
        if (arena.getState() != GlassMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new GlassTickEvent(arena));
    }
}
