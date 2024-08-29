package com.covercorp.kaelaevent.minigame.games.tug.arena.task;

import com.covercorp.kaelaevent.minigame.games.tug.arena.TugArena;
import com.covercorp.kaelaevent.minigame.games.tug.arena.event.TugTickEvent;
import com.covercorp.kaelaevent.minigame.games.tug.arena.state.TugMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TugTickTask implements Runnable {
    private final TugArena arena;

    @Override
    public void run() {
        if (arena.getState() != TugMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new TugTickEvent(arena));
    }
}
