package com.covercorp.kaelaevent.minigame.games.reflex.arena.task;

import com.covercorp.kaelaevent.minigame.games.reflex.arena.ReflexArena;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.event.ReflexCooldownEndEvent;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.state.ReflexMatchState;

import com.covercorp.kaelaevent.minigame.games.reflex.arena.state.ReflexRoundState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ReflexTimeTask implements Runnable {
    private final ReflexArena arena;

    public void run() {
        if (arena.getState() != ReflexMatchState.GAME) return;

        arena.setGameTime(arena.getGameTime() + 1);
        arena.getAnnouncer().sendGlobalSound("kaela:tick", 0.1F, 1.7F);

        // Button time
        if (arena.getReflexMatchProperties().getRoundState() != ReflexRoundState.WAITING) return;
        final int time = arena.getReflexMatchProperties().getPressCooldown();
        if (time <= 0) {
            arena.getReflexMatchProperties().setRoundState(ReflexRoundState.PRESS);
            Bukkit.getPluginManager().callEvent(new ReflexCooldownEndEvent(arena));

            return;
        }

        arena.getReflexMatchProperties().setPressCooldown(time - 1);
    }
}