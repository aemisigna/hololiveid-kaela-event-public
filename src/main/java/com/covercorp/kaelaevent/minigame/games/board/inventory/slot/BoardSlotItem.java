package com.covercorp.kaelaevent.minigame.games.board.inventory.slot;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public final class BoardSlotItem {
    private final ItemStack itemStack;
    private final String server;
}
