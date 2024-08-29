package com.covercorp.kaelaevent.minigame.games.zombie.arena.event;

import com.covercorp.kaelaevent.minigame.games.zombie.arena.ZombieArena;
import com.covercorp.kaelaevent.minigame.games.zombie.player.ZombiePlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ZombieKillEvent extends Event {
    private final static HandlerList handlers = new HandlerList();

    private final ZombieArena arena;
    private final ZombiePlayer player;

    private final Entity entity;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
