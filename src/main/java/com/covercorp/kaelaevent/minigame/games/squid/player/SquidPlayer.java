package com.covercorp.kaelaevent.minigame.games.squid.player;

import com.covercorp.kaelaevent.minigame.games.squid.SquidMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class SquidPlayer extends MiniGamePlayer<SquidMiniGame> {
    private boolean dead;
    private boolean flaggedToDeath;
    private boolean finished;
    private Instant finishTime;

    public SquidPlayer(final SquidMiniGame miniGame, final UUID uniqueId, final String name) {
        super(miniGame, uniqueId, name);
    }
}
