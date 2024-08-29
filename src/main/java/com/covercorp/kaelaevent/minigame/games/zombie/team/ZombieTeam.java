package com.covercorp.kaelaevent.minigame.games.zombie.team;

import com.covercorp.kaelaevent.minigame.games.zombie.player.ZombiePlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public final class ZombieTeam extends MiniGameTeam<ZombiePlayer> {
    public ZombieTeam(String identifier) {
        super(identifier);
    }

    public int getScore() {
        int score = 0;

        for (ZombiePlayer player : this.getPlayers()) {
            score += player.getScore();
        }

        return score;
    }

    public ZombiePlayer getFirstPlayer() {
        return getPlayers().stream().findFirst().orElse(null);
    }
}
