package com.covercorp.kaelaevent.minigame.games.trident.arena.task;

import com.covercorp.kaelaevent.minigame.games.trident.arena.TridentArena;
import com.covercorp.kaelaevent.minigame.games.trident.arena.event.TridentTickEvent;
import com.covercorp.kaelaevent.minigame.games.trident.arena.state.TridentMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TridentTickTask implements Runnable {
    private final TridentArena arena;

    @Override
    public void run() {
        if (arena.getState() != TridentMatchState.GAME) return;

        Bukkit.getServer().getPluginManager().callEvent(new TridentTickEvent(arena));
    }
}
