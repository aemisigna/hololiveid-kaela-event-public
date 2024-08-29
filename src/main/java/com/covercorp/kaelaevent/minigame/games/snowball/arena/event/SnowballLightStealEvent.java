package com.covercorp.kaelaevent.minigame.games.snowball.arena.event;

import com.covercorp.kaelaevent.minigame.games.snowball.arena.SnowballArena;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.light.light.side.LightSide;
import com.covercorp.kaelaevent.minigame.games.snowball.player.SnowballPlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class SnowballLightStealEvent extends Event {
    private final static HandlerList handlers = new HandlerList();

    private final SnowballArena arena;
    private final LightSide turnedOnLight;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
