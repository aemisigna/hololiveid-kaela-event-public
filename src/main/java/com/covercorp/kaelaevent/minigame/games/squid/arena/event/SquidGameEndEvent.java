package com.covercorp.kaelaevent.minigame.games.squid.arena.event;

import com.covercorp.kaelaevent.minigame.games.squid.arena.SquidArena;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class SquidGameEndEvent extends Event {
    private final static HandlerList handlers = new HandlerList();

    private final SquidArena arena;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
