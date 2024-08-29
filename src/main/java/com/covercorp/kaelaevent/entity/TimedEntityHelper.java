package com.covercorp.kaelaevent.entity;

import com.covercorp.kaelaevent.entity.timed.TimedEntity;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Entity;

import java.util.Optional;
import java.util.UUID;

public interface TimedEntityHelper {
    void makeEntity(final TimedEntity<? extends Entity> timedEntity, boolean override);
    Optional<TimedEntity<?>> getTimedEntity(final UUID uuid);
    void removeTimedEntity(final UUID uuid);
    ImmutableList<TimedEntity<? extends Entity>> getTimedEntities();
}
