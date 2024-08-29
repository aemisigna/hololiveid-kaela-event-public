package com.covercorp.kaelaevent.minigame.games.snowball.arena.light.light;

import com.covercorp.kaelaevent.minigame.games.snowball.arena.light.SnowballLightPair;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public final class SnowballLight {
    private final SnowballLightPair pair;
    private final Location location;

    private ItemDisplay targetDisplay;
    private Interaction targetHitbox;

    public void spawn() {
        if (isAlive()) {
            targetDisplay.remove();
            targetHitbox.remove();
        }

        final Location loc = location.clone();
        final Interaction hitbox = (Interaction)this.location.getWorld().spawnEntity(loc, EntityType.INTERACTION);
        NBTMetadataUtil.addStringToEntity(hitbox, "snowball_race", pair.getUniqueId().toString());

        hitbox.setInteractionHeight(1.8F);
        hitbox.setInteractionWidth(1.8F);

        this.targetHitbox = hitbox;

        final ItemDisplay targetDisplay = (ItemDisplay) location.getWorld().spawnEntity(loc.add(0.0, 0.8, 0.0), EntityType.ITEM_DISPLAY);
        targetDisplay.setItemStack(new ItemBuilder(Material.POPPED_CHORUS_FRUIT).withCustomModel(508).build());
        NBTMetadataUtil.addStringToEntity(targetDisplay, "snowball_race", targetDisplay.getUniqueId().toString());

        final Transformation transformation = targetDisplay.getTransformation();

        transformation.getScale().set(2.0, 2.0, 2.0);
        targetDisplay.setTransformation(transformation);

        this.targetDisplay = targetDisplay;
    }

    public void deSpawn() {
        if (!isAlive()) return;

        targetDisplay.remove();
        targetHitbox.remove();
    }

    public void light(final boolean light) {
        if (light) {
            targetDisplay.setGlowing(true);

            location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.9F, 0.9F);
            location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 10, 0.1, 0.1, 0.1);
            return;
        }

        targetDisplay.setGlowing(false);
        location.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, location, 10, 0.1, 0.1, 0.1);
    }

    public boolean isLit() {
        return targetDisplay.isGlowing();
    }

    public boolean isAlive() {
        return targetDisplay != null && !targetDisplay.isDead() && targetHitbox != null && !targetHitbox.isDead();
    }
}
