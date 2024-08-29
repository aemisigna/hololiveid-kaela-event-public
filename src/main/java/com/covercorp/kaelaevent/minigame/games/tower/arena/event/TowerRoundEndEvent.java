package com.covercorp.kaelaevent.minigame.games.tower.arena.event;

import com.covercorp.kaelaevent.minigame.games.tower.arena.TowerArena;
import com.covercorp.kaelaevent.minigame.games.tower.team.TowerTeam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TowerRoundEndEvent extends Event {
    private final static HandlerList handlers = new HandlerList();

    private final TowerArena arena;
    private final TowerTeam winnerTeam;
    private final TowerTeam loserTeam;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
