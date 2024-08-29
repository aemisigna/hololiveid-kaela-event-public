package com.covercorp.kaelaevent.minigame.games.squid.arena.task;

import com.covercorp.kaelaevent.minigame.games.squid.arena.SquidArena;
import com.covercorp.kaelaevent.minigame.games.squid.arena.event.SquidTickEvent;
import com.covercorp.kaelaevent.minigame.games.squid.arena.state.SquidMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class SquidTickTask implements Runnable {
    private final SquidArena arena;

    @Override
    public void run() {
        if (arena.getState() != SquidMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new SquidTickEvent(arena));
    }
}
