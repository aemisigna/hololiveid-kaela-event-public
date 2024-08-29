package com.covercorp.kaelaevent.minigame.games.zombie.arena.spot;

import com.covercorp.kaelaevent.minigame.games.zombie.arena.ZombieArena;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.spot.zombie.RangeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
public final class EntitySpot {
    private final ZombieArena arena;

    private final UUID uniqueId;
    private final Location startLocation;
    private final Location backLocation;

    private final List<RangeEntity> entities;

    private final static int SPAWNED_ENTITY_LIMIT = 1;

    public EntitySpot(final ZombieArena arena, final UUID uniqueId, final Location startLocation, final Location backLocation) {
        this.arena = arena;
        this.uniqueId = uniqueId;
        this.startLocation = startLocation;
        this.backLocation = backLocation;

        entities = new ArrayList<>();
    }

    public void summonEntity() {
        if (!isAvailable()) return;

        final RangeEntity rangeEntity = new RangeEntity(this);
        rangeEntity.spawn();

        entities.add(rangeEntity);
    }

    public void clearEntities() {
        entities.forEach(RangeEntity::deSpawn);
        entities.clear();
    }

    public boolean isAvailable() {
        int summonedEntities = entities.stream().filter(RangeEntity::isAlive).toList().size();

        return summonedEntities <= SPAWNED_ENTITY_LIMIT;
    }
}
