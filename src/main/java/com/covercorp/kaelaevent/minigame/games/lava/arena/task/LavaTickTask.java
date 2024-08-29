package com.covercorp.kaelaevent.minigame.games.lava.arena.task;

import com.covercorp.kaelaevent.minigame.games.lava.arena.LavaArena;
import com.covercorp.kaelaevent.minigame.games.lava.arena.event.LavaTickEvent;
import com.covercorp.kaelaevent.minigame.games.lava.arena.state.LavaMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class LavaTickTask implements Runnable {
    private final LavaArena arena;

    @Override
    public void run() {
        if (arena.getState() != LavaMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new LavaTickEvent(arena));
    }
}
