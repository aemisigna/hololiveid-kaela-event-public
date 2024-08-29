package com.covercorp.kaelaevent.minigame.games.tower.arena.spot.part;

import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.TowerSpot;
import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.part.status.GambleMachineStatus;
import com.covercorp.kaelaevent.minigame.games.tower.inventory.TowerItemCollection;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TowerGambleMachine {
    private final TowerSpot towerSpot;
    private final Location location;

    private GambleMachineStatus status;

    private ItemDisplay display;
    private Interaction hitbox;

    public void spawn() {
        deSpawn();

        final Interaction hitbox = (Interaction) location.getWorld().spawnEntity(location.clone().subtract(0.0, 1.0, 0.0), EntityType.INTERACTION);
        NBTMetadataUtil.addStringToEntity(hitbox, "gamble_machine", towerSpot.getUniqueId().toString());

        hitbox.setInteractionHeight(2.4F);
        hitbox.setInteractionWidth(1.4F);

        this.hitbox = hitbox;

        final ItemDisplay display = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        display.setItemStack(TowerItemCollection.GAMBLING_MACHINE_DEFAULT_LEVER_UP);
        NBTMetadataUtil.addStringToEntity(display, "gamble_machine", towerSpot.getUniqueId().toString());

        final Transformation transformation = display.getTransformation();

        transformation.getScale().set(1.0, 1.0, 1.0);
        display.setTransformation(transformation);

        this.display = display;
    }

    public void deSpawn() {
        if (!isAlive()) return;

        hitbox.remove();
        display.remove();
    }

    public void setStatus(final GambleMachineStatus status) {
        if (status == GambleMachineStatus.NO_ROLL) {
            display.setItemStack(TowerItemCollection.GAMBLING_MACHINE_DEFAULT_LEVER_UP);
        }
        if (status == GambleMachineStatus.ROLL) {
            display.setItemStack(TowerItemCollection.GAMBLING_MACHINE_ROLLING);
        }
        if (status == GambleMachineStatus.REDSTONE) {
            display.setItemStack(TowerItemCollection.GAMBLING_MACHINE_REDSTONE);
        }
        if (status == GambleMachineStatus.GOLD) {
            display.setItemStack(TowerItemCollection.GAMBLING_MACHINE_GOLD);
        }
        if (status == GambleMachineStatus.EMERALD) {
            display.setItemStack(TowerItemCollection.GAMBLING_MACHINE_EMERALD);
        }
        if (status == GambleMachineStatus.DIAMOND) {
            display.setItemStack(TowerItemCollection.GAMBLING_MACHINE_DIAMOND);
        }
        if (status == GambleMachineStatus.NETHERITE) {
            display.setItemStack(TowerItemCollection.GAMBLING_MACHINE_NETHERITE);
        }

        this.status = status;
    }

    public GambleMachineStatus getRandomStatus() {
        final List<GambleMachineStatus> statuses = Arrays.asList(
                GambleMachineStatus.REDSTONE,
                GambleMachineStatus.GOLD,
                GambleMachineStatus.EMERALD,
                GambleMachineStatus.DIAMOND,
                GambleMachineStatus.NETHERITE
        );

        Collections.shuffle(statuses);

        return statuses.get(new Random().nextInt(statuses.size()));
    }

    public boolean isRolled() {
        final List<GambleMachineStatus> statuses = Arrays.asList(
                GambleMachineStatus.REDSTONE,
                GambleMachineStatus.GOLD,
                GambleMachineStatus.EMERALD,
                GambleMachineStatus.DIAMOND,
                GambleMachineStatus.NETHERITE
        );

        return statuses.contains(getStatus());
    }

    public boolean isAlive() {
        return display != null && !display.isDead() && hitbox != null && !hitbox.isDead();
    }
}
