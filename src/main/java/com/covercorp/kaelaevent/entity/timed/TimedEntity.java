package com.covercorp.kaelaevent.entity.timed;

import lombok.*;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public abstract class TimedEntity<T extends Entity> {
    private final T entity;

    private Location spawnLocation;

    private final int baseTime;
    private int remainingTime; // Defaults to zero

    private boolean killed;

    public void spawn() {
        remainingTime = baseTime; // Set the time
        spawnLocation.getWorld().spawnEntity(spawnLocation, entity.getType());
    }

    public void tick() {
        remainingTime--;
    }

    public void deSpawn() {
        if (!entity.isDead()) entity.remove();
        killed = true;
    }
}