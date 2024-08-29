package com.covercorp.kaelaevent.entity.task;

import com.covercorp.kaelaevent.entity.TimedEntityHelper;

public final class TimedEntityTickTask implements Runnable {
    private final TimedEntityHelper timedEntityHelper;

    public TimedEntityTickTask(final TimedEntityHelper timedEntityHelper) {
        this.timedEntityHelper = timedEntityHelper;
    }

    @Override
    public void run() {
        timedEntityHelper.getTimedEntities().forEach(timedEntity -> {
            // Check if the entity is already despawned, this helps in case the entity finished its work before the timer ends
            if (timedEntity.isKilled()) {
                return;
            }

            // Tick the entity to decrease the remaining timer
            timedEntity.tick();

            if (timedEntity.getRemainingTime() <= 0) {
                timedEntityHelper.removeTimedEntity(timedEntity.getEntity().getUniqueId());
            }
        });
    }
}
