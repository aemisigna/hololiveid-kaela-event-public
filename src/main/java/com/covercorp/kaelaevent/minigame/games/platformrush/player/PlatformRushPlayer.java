package com.covercorp.kaelaevent.minigame.games.platformrush.player;

import com.covercorp.kaelaevent.minigame.games.platformrush.PlatformRushMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class PlatformRushPlayer extends MiniGamePlayer<PlatformRushMiniGame> {
    private int blocksBroken;
    private boolean dead;

    public PlatformRushPlayer(final PlatformRushMiniGame miniGame, final UUID uniqueId, final String name) {
        super(miniGame, uniqueId, name);
    }
}
