package com.covercorp.kaelaevent.minigame.games.snowball.arena.machine;

import com.covercorp.kaelaevent.minigame.games.snowball.arena.SnowballArena;
import com.covercorp.kaelaevent.minigame.games.snowball.inventory.SnowballItemCollection;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class ScoreMachine {
    private final SnowballArena snowballArena;
    private final Location location;

    private ItemDisplay display;

    public ScoreMachine(final SnowballArena snowballArena, final Location location) {
        this.snowballArena = snowballArena;
        this.location = location;
    }

    public void spawn() {
        if (isAlive()) display.remove();

        final Location loc = this.location.clone();

        final ItemDisplay display = (ItemDisplay)this.location.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY);

        display.setItemStack(SnowballItemCollection.SCORE_MACHINE_3);

        NBTMetadataUtil.addStringToEntity(display, "snowball_race", display.getUniqueId().toString());
        NBTMetadataUtil.addStringToEntity(display, "score_machine", display.getUniqueId().toString());

        final Transformation transformation = display.getTransformation();

        transformation.getScale().set(3.0, 3.0, 3.0);
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

    public void changeModel(final int score) {
        switch (score) {
            case 0 -> display.setItemStack(SnowballItemCollection.SCORE_MACHINE_0);
            case 1 -> display.setItemStack(SnowballItemCollection.SCORE_MACHINE_1);
            case 2 -> display.setItemStack(SnowballItemCollection.SCORE_MACHINE_2);
            case 3 -> display.setItemStack(SnowballItemCollection.SCORE_MACHINE_3);
            case 4 -> display.setItemStack(SnowballItemCollection.SCORE_MACHINE_4);
            case 5 -> display.setItemStack(SnowballItemCollection.SCORE_MACHINE_5);
            case 6 -> display.setItemStack(SnowballItemCollection.SCORE_MACHINE_6);
        }
    }
}
