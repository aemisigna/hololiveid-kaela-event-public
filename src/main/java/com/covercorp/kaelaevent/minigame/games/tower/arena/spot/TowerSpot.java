package com.covercorp.kaelaevent.minigame.games.tower.arena.spot;

import com.covercorp.kaelaevent.minigame.games.tower.arena.TowerArena;
import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.part.TowerGambleMachine;
import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.part.status.GambleMachineStatus;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;

import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
public final class TowerSpot {
    private final UUID uniqueId;

    private final Location spawnLocation;
    private final TowerGambleMachine gambleMachine;

    public TowerSpot(final TowerArena arena, final String spotId) {
        this.uniqueId = UUID.randomUUID();

        spawnLocation = arena.getTowerMiniGame().getConfigHelper().getSpotSpawnLocation(spotId);
        gambleMachine = new TowerGambleMachine(this, arena.getTowerMiniGame().getConfigHelper().getSpotGambleMachine(spotId));
    }

    public void spawnParts() {
        gambleMachine.spawn();
    }

    public void deSpawnParts() {
        gambleMachine.deSpawn();
    }

    public void setGambleMachineStatus(final GambleMachineStatus status) {
        if (gambleMachine == null) return;

        gambleMachine.setStatus(status);
    }
}
