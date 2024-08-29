package com.covercorp.kaelaevent.minigame.games.reflex.arena.event;

import com.covercorp.kaelaevent.minigame.games.reflex.arena.ReflexArena;
import com.covercorp.kaelaevent.minigame.games.reflex.team.ReflexTeam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ReflexRoundEndEvent extends Event {
    private final static HandlerList handlers = new HandlerList();

    private final ReflexArena arena;
    private final ReflexTeam loserTeam;
    private final ReflexTeam winnerTeam;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
