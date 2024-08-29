package com.covercorp.kaelaevent.minigame.games.zombie.arena.task;

import com.covercorp.kaelaevent.minigame.games.zombie.arena.ZombieArena;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.event.ZombieTickEvent;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.state.ZombieMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ZombieTickTask implements Runnable {
    private final ZombieArena arena;

    @Override
    public void run() {
        if (arena.getState() != ZombieMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new ZombieTickEvent(arena));
    }
}
