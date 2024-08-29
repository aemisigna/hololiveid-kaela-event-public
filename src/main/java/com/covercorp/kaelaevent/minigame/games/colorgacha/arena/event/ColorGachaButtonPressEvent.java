package com.covercorp.kaelaevent.minigame.games.colorgacha.arena.event;

import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.ColorGachaArena;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.ColorGachaButton;
import com.covercorp.kaelaevent.minigame.games.colorgacha.player.ColorGachaPlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ColorGachaButtonPressEvent extends Event {
    private final static HandlerList handlers = new HandlerList();

    private final ColorGachaArena arena;
    private final ColorGachaPlayer talent;
    private final ColorGachaButton button;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
