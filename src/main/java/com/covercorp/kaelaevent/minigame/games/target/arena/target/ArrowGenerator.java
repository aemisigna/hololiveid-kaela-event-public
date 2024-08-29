package com.covercorp.kaelaevent.minigame.games.target.arena.target;

import java.util.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.*;
import org.bukkit.*;
import com.covercorp.kaelaevent.util.*;
import org.bukkit.util.*;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class ArrowGenerator {
    private final UUID uniqueId;
    private final Location center;
    private final Location location;
    private Interaction hitbox;
    private ItemDisplay display;

    public ArrowGenerator(final UUID uniqueId, final Location center, final Location location) {
        this.uniqueId = uniqueId;
        this.center = center;
        this.location = location;
    }

    public void spawn() {
        if (this.hitbox != null && !this.hitbox.isDead()) {
            this.hitbox.remove();
        }
        if (this.display != null && !this.display.isDead()) {
            this.display.remove();
        }

        final Location loc = this.location.clone();
        final Interaction hitbox = (Interaction)this.location.getWorld().spawnEntity(loc, EntityType.INTERACTION);
        NBTMetadataUtil.addStringToEntity(hitbox, "arrow_gen", this.uniqueId.toString());

        hitbox.setInteractionHeight(3.0f);
        hitbox.setInteractionWidth(3.0f);

        this.hitbox = hitbox;

        final ItemDisplay display = (ItemDisplay)this.location.getWorld().spawnEntity(loc.add(0.0, 1.25, 0.0), EntityType.ITEM_DISPLAY);

        display.setGlowing(true);
        display.setGlowColorOverride(Color.AQUA);
        display.setItemStack(new ItemBuilder(Material.POPPED_CHORUS_FRUIT).withCustomModel(46).build());

        NBTMetadataUtil.addStringToEntity(display, "arrow_gen", this.uniqueId.toString());

        final Transformation transformation = display.getTransformation();

        transformation.getScale().set(3.0, 3.0, 3.0);
        display.setTransformation(transformation);

        this.display = display;
    }

    public void deSpawn() {
        if (this.hitbox != null) {
            this.hitbox.remove();
            this.hitbox = null;
        }
        if (this.display != null) {
            this.display.remove();
            this.display = null;
        }
    }
}
