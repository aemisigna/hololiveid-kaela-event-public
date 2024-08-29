package com.covercorp.kaelaevent.minigame.games.lava.arena.slot.slot;

import com.covercorp.kaelaevent.minigame.games.lava.arena.slot.slot.state.SlotStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public final class LavaSlot {
    private final String identifier;

    private final Location center;
    private final List<Location> blocks;

    private SlotStatus status;

    public void setStatus(final SlotStatus status) {
        blocks.forEach(blockLoc -> {
            final Block block = blockLoc.getBlock();
            switch (status) {
                case SAFE -> block.setType(Material.LIME_CONCRETE);
                case WARNING -> block.setType(Material.YELLOW_CONCRETE);
                case LAVA -> block.setType(Material.RED_CONCRETE);
            }
        });

        this.status = status;
    }
}
