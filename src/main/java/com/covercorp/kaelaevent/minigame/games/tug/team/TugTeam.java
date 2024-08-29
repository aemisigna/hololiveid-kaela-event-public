package com.covercorp.kaelaevent.minigame.games.tug.team;

import com.covercorp.kaelaevent.minigame.games.tug.player.TugPlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public final class TugTeam extends MiniGameTeam<TugPlayer> {
    private Location spawnPoint;

    public TugTeam(String identifier) {
        super(identifier);
    }

    public int getTeamScore() {
        int points = 0;

        for (final TugPlayer player : getPlayers()) {
            points += player.getScore();
        }

        return points;
    }
}
