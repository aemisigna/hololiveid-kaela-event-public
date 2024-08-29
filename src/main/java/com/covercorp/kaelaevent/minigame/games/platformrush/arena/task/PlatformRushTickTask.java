package com.covercorp.kaelaevent.minigame.games.platformrush.arena.task;

import com.covercorp.kaelaevent.minigame.games.platformrush.arena.PlatformRushArena;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.event.PlatformRushTickEvent;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.state.PlatformRushMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class PlatformRushTickTask implements Runnable {
    private final PlatformRushArena arena;

    @Override
    public void run() {
        if (arena.getState() != PlatformRushMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new PlatformRushTickEvent(arena));
    }
}
