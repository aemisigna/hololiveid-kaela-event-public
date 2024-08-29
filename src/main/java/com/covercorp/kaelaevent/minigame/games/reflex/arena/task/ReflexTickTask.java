package com.covercorp.kaelaevent.minigame.games.reflex.arena.task;

import com.covercorp.kaelaevent.minigame.games.reflex.arena.ReflexArena;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.event.ReflexTickEvent;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.state.ReflexMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ReflexTickTask implements Runnable {
    private final ReflexArena arena;

    @Override
    public void run() {
        if (arena.getState() != ReflexMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new ReflexTickEvent(arena));
    }
}
