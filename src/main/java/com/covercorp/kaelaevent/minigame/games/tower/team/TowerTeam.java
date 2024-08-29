package com.covercorp.kaelaevent.minigame.games.tower.team;

import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.TowerSpot;
import com.covercorp.kaelaevent.minigame.games.tower.player.TowerPlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class TowerTeam extends MiniGameTeam<TowerPlayer> {
    private TowerSpot towerSpot;
    private int score;

    public TowerTeam(final String identifier) {
        super(identifier);
    }

    public TowerPlayer getFirstPlayer() {
        return getPlayers().stream().findFirst().orElse(null);
    }
}