package com.covercorp.kaelaevent.minigame.games.basketball.arena.task;

import com.covercorp.kaelaevent.minigame.games.basketball.arena.BasketballArena;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.event.BasketballTickEvent;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.state.BasketballMatchState;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BasketballTickTask implements Runnable {
    private final BasketballArena arena;

    @Override
    public void run() {
        if (arena.getState() != BasketballMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new BasketballTickEvent(arena));
    }
}
