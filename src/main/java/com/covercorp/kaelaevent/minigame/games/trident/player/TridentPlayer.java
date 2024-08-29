package com.covercorp.kaelaevent.minigame.games.trident.player;

import com.covercorp.kaelaevent.minigame.games.trident.TridentMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class TridentPlayer extends MiniGamePlayer<TridentMiniGame> {
    private int score;

    public TridentPlayer(TridentMiniGame miniGame, UUID uniqueId, String name) {
        super(miniGame, uniqueId, name);
    }
}
