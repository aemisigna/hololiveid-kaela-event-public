package com.covercorp.kaelaevent.minigame.games.trident.arena.target;

import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class TridentTarget {
    private final UUID uniqueId;
    private final Location center;
    private final Location location;

    private Interaction hitbox;
    private ItemDisplay display;

    public TridentTarget(final UUID uniqueId, final Location center, final Location location) {
        this.uniqueId = uniqueId;
        this.center = center;
        this.location = location;
    }

    public void spawn() {
        if (hitbox != null && !hitbox.isDead()) {
            hitbox.remove();
        }
        if (display != null && !display.isDead()) {
            display.remove();
        }

        final Location loc = location.clone();
        final Interaction hitbox = (Interaction)this.location.getWorld().spawnEntity(loc, EntityType.INTERACTION);
        NBTMetadataUtil.addStringToEntity(hitbox, "target_id", this.uniqueId.toString());

        hitbox.setInteractionHeight(2.4f);
        hitbox.setInteractionWidth(2.4f);

        this.hitbox = hitbox;

        final ItemDisplay display = (ItemDisplay) location.getWorld().spawnEntity(loc.add(0.0, 0.8, 0.0), EntityType.ITEM_DISPLAY);
        display.setGlowing(true);
        display.setGlowColorOverride(Color.BLUE);
        display.setItemStack(new ItemBuilder(Material.POPPED_CHORUS_FRUIT).withCustomModel(508).build());
        NBTMetadataUtil.addStringToEntity(display, "target_id", this.uniqueId.toString());

        location.getWorld().spawnParticle(Particle.CLOUD, location, 10, 0.0, 0.0, 0.0);
        location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.5f, 1.5f);

        final Vector direction = center.toVector().subtract(location.toVector()).normalize();
        final Location locDir = location.clone();

        locDir.setDirection(direction);

        final Transformation transformation = display.getTransformation();
        transformation.getScale().set(2.4, 2.4, 2.4);
        display.setTransformation(transformation);

        this.display = display;

        this.hitbox.setRotation(locDir.getYaw(), locDir.getPitch());
        this.display.setRotation(locDir.getYaw(), locDir.getPitch());
    }

    public void deSpawn() {
        if (hitbox != null) {
            hitbox.remove();
            hitbox = null;
        }
        if (display != null) {
            display.remove();
            display = null;
        }
    }
}