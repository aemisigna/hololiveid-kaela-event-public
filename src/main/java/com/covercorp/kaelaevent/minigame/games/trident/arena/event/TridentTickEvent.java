package com.covercorp.kaelaevent.minigame.games.trident.arena.event;

import com.covercorp.kaelaevent.minigame.games.trident.arena.TridentArena;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TridentTickEvent extends Event {
    private final static HandlerList handlers = new HandlerList();

    private final TridentArena arena;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
