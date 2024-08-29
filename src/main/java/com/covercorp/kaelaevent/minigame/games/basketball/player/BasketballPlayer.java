package com.covercorp.kaelaevent.minigame.games.basketball.player;

import com.covercorp.kaelaevent.minigame.games.basketball.BasketballMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class BasketballPlayer extends MiniGamePlayer<BasketballMiniGame> {
    private int score;

    public BasketballPlayer(final BasketballMiniGame miniGame, final UUID uniqueId, final String name) {
        super(miniGame, uniqueId, name);
    }
}
