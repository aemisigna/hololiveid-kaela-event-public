package com.covercorp.kaelaevent.minigame.games.tower.arena.event;

import com.covercorp.kaelaevent.minigame.games.tower.arena.TowerArena;
import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.TowerSpot;
import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.part.status.GambleMachineStatus;
import com.covercorp.kaelaevent.minigame.games.tower.player.TowerPlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TowerGambleRollEndEvent extends Event {
    private final static HandlerList handlers = new HandlerList();

    private final TowerArena arena;
    private final TowerPlayer player;
    private final TowerSpot spot;
    private final GambleMachineStatus selectedStatus;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
