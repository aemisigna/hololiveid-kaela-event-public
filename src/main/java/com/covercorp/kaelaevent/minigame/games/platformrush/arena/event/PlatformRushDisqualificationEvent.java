package com.covercorp.kaelaevent.minigame.games.platformrush.arena.event;

import com.covercorp.kaelaevent.minigame.games.platformrush.arena.PlatformRushArena;
import com.covercorp.kaelaevent.minigame.games.platformrush.player.PlatformRushPlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class PlatformRushDisqualificationEvent extends Event {
    private final static HandlerList handlers = new HandlerList();

    private final PlatformRushArena arena;
    private final PlatformRushPlayer disqualifiedPlayer;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
