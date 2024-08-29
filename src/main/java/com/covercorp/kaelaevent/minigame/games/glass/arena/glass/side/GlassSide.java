package com.covercorp.kaelaevent.minigame.games.glass.arena.glass.side;

import com.covercorp.kaelaevent.minigame.games.glass.arena.glass.BridgePart;
import com.covercorp.kaelaevent.util.BlockUtils;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;

@Getter(AccessLevel.PUBLIC)
public final class GlassSide {
    private final BridgePart bridgePart;
    private final Location center;
    private final List<Block> blocks;

    @Setter(AccessLevel.PUBLIC) private Interaction hitbox;
    @Setter(AccessLevel.PUBLIC) private ItemDisplay display;

    @Setter(AccessLevel.PUBLIC) private boolean evil;

    public GlassSide(BridgePart part, Location center) {
        this.bridgePart = part;
        this.center = center;
        this.blocks = BlockUtils.getAdjacentBlocks(center);
    }

    public void spawn() {
        deSpawn();

        blocks.forEach(block -> {
            Location locx = block.getLocation().clone();
            locx.subtract(0.0, 1.0, 0.0);
            locx.getBlock().setType(Material.BARRIER);
        });

        final Location loc = this.center.clone();
        final Interaction hitbox = (Interaction) this.center.getWorld().spawnEntity(loc, EntityType.INTERACTION);
        NBTMetadataUtil.addStringToEntity(hitbox, "bridge_glass", this.bridgePart.getId());

        hitbox.setInteractionHeight(2.1F);
        hitbox.setInteractionWidth(3.0F);

        this.hitbox = hitbox;

        final ItemDisplay display = (ItemDisplay)this.center.getWorld().spawnEntity(loc.add(0.0, 1.0, 0.0), EntityType.ITEM_DISPLAY);
        display.setItemStack(new ItemBuilder(Material.POPPED_CHORUS_FRUIT).withCustomModel(19).build());
        NBTMetadataUtil.addStringToEntity(display, "bridge_glass", this.bridgePart.getId());

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

        this.blocks.forEach(block -> {
            Location loc = block.getLocation().clone();
            loc.subtract(0.0, 1.0, 0.0);
            loc.getBlock().setType(Material.AIR);
        });
    }

    public void breakGlass(boolean sound) {
        if (this.hitbox != null) {
            this.hitbox.remove();
            this.hitbox = null;
        }

        if (this.display != null) {
            this.display.setItemStack(new ItemBuilder(Material.POPPED_CHORUS_FRUIT).withCustomModel(20).build());
        }

        this.blocks.forEach(block -> {
            Location loc = block.getLocation().clone();
            if (sound) {
                loc.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.GLASS);
            }

            loc.subtract(0.0, 1.0, 0.0);
            loc.getBlock().setType(Material.AIR);
        });
    }

    public boolean isAlive() {
        return this.hitbox != null && !this.hitbox.isDead();
    }
}