package com.covercorp.kaelaevent.entity;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.entity.task.TimedEntityTickTask;
import com.covercorp.kaelaevent.entity.timed.TimedEntity;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TimedEntityHelperImpl implements TimedEntityHelper {
    private final KaelaEvent kaelaEvent;

    private final Map<UUID, TimedEntity<? extends Entity>> timedEntityMap;

    public TimedEntityHelperImpl(final KaelaEvent kaelaEvent) {
        this.kaelaEvent = kaelaEvent;

        timedEntityMap = new ConcurrentHashMap<>();

        kaelaEvent.getServer().getScheduler().runTaskTimer(kaelaEvent, new TimedEntityTickTask(this), 0L, 1L);
    }

    public synchronized void makeEntity(final TimedEntity<? extends Entity> timedEntity, boolean override) {
        // Check if entity is already registered
        final UUID registeringEntityUniqueId = timedEntity.getEntity().getUniqueId();
        final Optional<TimedEntity<?>> possibleEntity = getTimedEntity(registeringEntityUniqueId);
        if (possibleEntity.isPresent() && override) {
            final TimedEntity<?> overridenEntity = possibleEntity.get();
            overridenEntity.deSpawn();
        }

        timedEntityMap.put(registeringEntityUniqueId, timedEntity);
        //timedEntity.spawn();
    }

    public Optional<TimedEntity<?>> getTimedEntity(final UUID uuid) {
        return Optional.ofNullable(timedEntityMap.get(uuid));
    }

    public synchronized void removeTimedEntity(final UUID uuid) {
        final TimedEntity<?> removedEntity = timedEntityMap.remove(uuid);
        if (removedEntity != null) {
            removedEntity.deSpawn();
        }
    }

    public ImmutableList<TimedEntity<? extends Entity>> getTimedEntities() {
        return ImmutableList.copyOf(timedEntityMap.values());
    }
}
