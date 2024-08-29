package com.covercorp.kaelaevent.minigame.games.snowball.arena.machine;

import com.covercorp.kaelaevent.minigame.games.snowball.arena.SnowballArena;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.*;
import org.bukkit.*;
import com.covercorp.kaelaevent.util.*;
import org.bukkit.util.*;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class IceMachine {
    private final SnowballArena snowballArena;
    private final Location location;

    private Interaction hitbox;
    private ItemDisplay display;

    public IceMachine(final SnowballArena snowballArena, final Location location) {
        this.snowballArena = snowballArena;
        this.location = location;
    }

    public void spawn() {
        if (isAlive()) {
            display.remove();
            hitbox.remove();
        }

        final Location loc = location.clone();
        final Interaction hitbox = (Interaction)this.location.getWorld().spawnEntity(loc, EntityType.INTERACTION);
        NBTMetadataUtil.addStringToEntity(hitbox, "snowball_race", hitbox.getUniqueId().toString());
        NBTMetadataUtil.addStringToEntity(hitbox, "ice_machine", hitbox.getUniqueId().toString());

        hitbox.setInteractionHeight(1.3F);
        hitbox.setInteractionWidth(1.3F);

        this.hitbox = hitbox;

        final ItemDisplay display = (ItemDisplay)this.location.getWorld().spawnEntity(loc.add(0.0, 0.8, 0.0), EntityType.ITEM_DISPLAY);

        display.setGlowing(true);
        display.setGlowColorOverride(Color.AQUA);
        display.setItemStack(new ItemBuilder(Material.POPPED_CHORUS_FRUIT).withCustomModel(58).build());

        NBTMetadataUtil.addStringToEntity(display, "snowball_race", display.getUniqueId().toString());
        NBTMetadataUtil.addStringToEntity(display, "ice_machine", display.getUniqueId().toString());

        final Transformation transformation = display.getTransformation();

        transformation.getScale().set(1.1, 1.1, 1.2);
        display.setTransformation(transformation);

        this.display = display;
    }

    public void deSpawn() {
        if (!isAlive()) return;

        display.remove();
        hitbox.remove();
    }

    public boolean isAlive() {
        return display != null && !display.isDead() &&
                hitbox != null && !hitbox.isDead();
    }
}
