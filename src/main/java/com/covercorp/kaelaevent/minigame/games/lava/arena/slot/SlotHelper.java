package com.covercorp.kaelaevent.minigame.games.lava.arena.slot;

import com.covercorp.kaelaevent.minigame.games.lava.arena.LavaArena;
import com.covercorp.kaelaevent.minigame.games.lava.arena.slot.slot.LavaSlot;
import com.covercorp.kaelaevent.minigame.games.lava.arena.slot.slot.state.SlotStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SlotHelper {
    public final LavaArena arena;

    private final Map<String, LavaSlot> slots;

    public SlotHelper(final LavaArena arena) {
        this.arena = arena;

        slots = new ConcurrentHashMap<>();
    }

    public void addSlot(final @NotNull LavaSlot slot) {
        slot.setStatus(SlotStatus.SAFE);

        slots.put(slot.getIdentifier(), slot);

        arena.getLavaMiniGame().getKaelaEvent().getLogger().info("Added slot: " + slot.getIdentifier());
    }

    public LavaSlot getSlot(final String standIdentifier) {
        return slots.get(standIdentifier);
    }

    public List<LavaSlot> getSlots() {
        return new ArrayList<>(slots.values());
    }
}
