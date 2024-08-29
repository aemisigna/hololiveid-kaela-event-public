package com.covercorp.kaelaevent.minigame.games.snowball.team;

import com.covercorp.kaelaevent.minigame.games.snowball.arena.machine.IceMachine;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.machine.ScoreMachine;
import com.covercorp.kaelaevent.minigame.games.snowball.player.SnowballPlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class SnowballTeam extends MiniGameTeam<SnowballPlayer> {
    private Location spawnPoint;
    private IceMachine iceMachine;
    private ScoreMachine scoreMachine;

    private int score = 3;

    public SnowballTeam(final String identifier) {
        super(identifier);
    }
    
    public SnowballPlayer getFirstPlayer() {
        return getPlayers().stream().findFirst().orElse(null);
    }

    public void setScore(final int score) {
        this.score = score;

        if (scoreMachine.isAlive()) scoreMachine.changeModel(score);
    }

    public void increaseScore() {
        score++;

        if (scoreMachine.isAlive()) scoreMachine.changeModel(score);
    }

    public void decreaseScore() {
        score--;

        if (scoreMachine.isAlive()) scoreMachine.changeModel(score);
    }
}