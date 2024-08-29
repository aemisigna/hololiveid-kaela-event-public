package com.covercorp.kaelaevent.minigame.games.tower.arena.task;

import com.covercorp.kaelaevent.minigame.games.tower.arena.TowerArena;
import com.covercorp.kaelaevent.minigame.games.tower.arena.event.TowerTickEvent;
import com.covercorp.kaelaevent.minigame.games.tower.arena.state.TowerMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TowerTickTask implements Runnable {
    private final TowerArena arena;

    @Override
    public void run() {
        if (arena.getState() != TowerMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new TowerTickEvent(arena));
    }
}
