package com.covercorp.kaelaevent.minigame.games.target.player;

import com.covercorp.kaelaevent.minigame.games.target.TargetMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class TargetPlayer extends MiniGamePlayer<TargetMiniGame> {
    private int score;

    public TargetPlayer(TargetMiniGame miniGame, UUID uniqueId, String name) {
        super(miniGame, uniqueId, name);
    }
}
