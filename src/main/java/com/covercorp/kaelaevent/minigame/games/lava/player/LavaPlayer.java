package com.covercorp.kaelaevent.minigame.games.lava.player;

import com.covercorp.kaelaevent.minigame.games.lava.LavaMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class LavaPlayer extends MiniGamePlayer<LavaMiniGame> {
    public boolean dead;

    public LavaPlayer(final LavaMiniGame miniGame, final UUID uniqueId, final String name) {
        super(miniGame, uniqueId, name);
    }
}
