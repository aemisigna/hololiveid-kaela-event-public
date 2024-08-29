package com.covercorp.kaelaevent.minigame.games.snowball.arena.task;

import com.covercorp.kaelaevent.minigame.games.snowball.arena.SnowballArena;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.event.SnowballTickEvent;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.state.SnowballMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class SnowballTickTask implements Runnable {
    private final SnowballArena arena;

    @Override
    public void run() {
        if (arena.getState() != SnowballMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new SnowballTickEvent(arena));
    }
}
