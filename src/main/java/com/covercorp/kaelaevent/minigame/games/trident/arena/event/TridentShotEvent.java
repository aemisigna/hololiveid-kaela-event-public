package com.covercorp.kaelaevent.minigame.games.trident.arena.event;

import com.covercorp.kaelaevent.minigame.games.trident.arena.TridentArena;
import com.covercorp.kaelaevent.minigame.games.trident.arena.target.TridentTarget;
import com.covercorp.kaelaevent.minigame.games.trident.player.TridentPlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TridentShotEvent extends Event {
    private final static HandlerList handlers = new HandlerList();

    private final TridentArena arena;
    private final TridentPlayer tridentPlayer;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
