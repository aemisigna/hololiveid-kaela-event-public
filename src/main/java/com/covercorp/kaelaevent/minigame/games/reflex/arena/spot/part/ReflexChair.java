package com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.part;

import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.ReflexSpot;
import com.covercorp.kaelaevent.minigame.games.reflex.inventory.ReflexItemCollection;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ReflexChair {
    private final ReflexSpot reflexSpot;
    private final Location location;

    private ItemDisplay display;

    public void spawn() {
        deSpawn();

        final ItemDisplay display = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        display.setItemStack(ReflexItemCollection.REFLEX_CHAIR);
        NBTMetadataUtil.addStringToEntity(display, "reflex_chair", display.getUniqueId().toString());

        final Transformation transformation = display.getTransformation();
        transformation.getScale().set(0.9, 0.9, 0.9);
        display.setTransformation(transformation);

        this.display = display;
    }

    public void deSpawn() {
        if (!isAlive()) return;

        display.remove();
    }

    public boolean isAlive() {
        return display != null && !display.isDead();
    }
}
