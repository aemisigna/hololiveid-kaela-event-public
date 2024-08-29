package com.covercorp.kaelaevent.minigame.games.reflex.player;

import com.covercorp.kaelaevent.minigame.games.reflex.ReflexMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class ReflexPlayer extends MiniGamePlayer<ReflexMiniGame> {
    public ReflexPlayer(final ReflexMiniGame miniGame, final UUID uniqueId, final String name) {
        super(miniGame, uniqueId, name);
    }
}