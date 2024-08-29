package com.covercorp.kaelaevent.minigame.games.target.team;

import com.covercorp.kaelaevent.minigame.games.target.player.TargetPlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public final class TargetTeam extends MiniGameTeam<TargetPlayer> {
    public TargetTeam(String identifier) {
        super(identifier);
    }

    public int getScore() {
        int score = 0;

        for (TargetPlayer player : this.getPlayers()) {
            score += player.getScore();
        }

        return score;
    }
}
