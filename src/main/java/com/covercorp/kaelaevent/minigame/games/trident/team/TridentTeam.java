package com.covercorp.kaelaevent.minigame.games.trident.team;

import com.covercorp.kaelaevent.minigame.games.trident.player.TridentPlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public final class TridentTeam extends MiniGameTeam<TridentPlayer> {
    public TridentTeam(String identifier) {
        super(identifier);
    }

    public int getScore() {
        int score = 0;

        for (TridentPlayer player : this.getPlayers()) {
            score += player.getScore();
        }

        return score;
    }

    public TridentPlayer getFirstPlayer() {
        return getPlayers().stream().findFirst().orElse(null);
    }
}
