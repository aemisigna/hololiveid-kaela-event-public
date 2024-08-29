package com.covercorp.kaelaevent.minigame.games.target.arena.task;

import com.covercorp.kaelaevent.minigame.games.target.arena.TargetArena;
import com.covercorp.kaelaevent.minigame.games.target.arena.event.TargetTickEvent;
import com.covercorp.kaelaevent.minigame.games.target.arena.state.TargetMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TargetTickTask implements Runnable {
    private final TargetArena arena;

    @Override
    public void run() {
        if (arena.getState() != TargetMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new TargetTickEvent(arena));
    }
}
