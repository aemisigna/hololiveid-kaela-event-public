package com.covercorp.kaelaevent.minigame.games.squid.team;

import com.covercorp.kaelaevent.minigame.games.squid.player.SquidPlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public final class SquidTeam extends MiniGameTeam<SquidPlayer> {
    public SquidTeam(String identifier) {
        super(identifier);
    }

    public double getAverageFinishTime(final Instant arenaStartTime) {
        final List<SquidPlayer> players = this.getPlayers().stream().filter(Objects::nonNull).map(SquidPlayer.class::cast).filter(SquidPlayer::isFinished).toList();
        if (players.isEmpty()) {
            return Double.MAX_VALUE;
        } else {
            final OptionalDouble averageFinishTime = players.stream()
                    .mapToDouble(player -> (double)(player.getFinishTime().getEpochSecond() - arenaStartTime.getEpochSecond()))
                    .average();
            return averageFinishTime.orElse(Double.MAX_VALUE);
        }
    }

    public int getAlivePlayers() {
        int alive = 0;

        for (SquidPlayer squidPlayer : this.getPlayers()) {
            if (!squidPlayer.isDead()) {
                alive++;
            }
        }

        return alive;
    }

    public int getFinishedPlayers() {
        int finished = 0;

        for (SquidPlayer squidPlayer : this.getPlayers()) {
            if (squidPlayer.isFinished()) {
                finished++;
            }
        }

        return finished;
    }
}
