package com.covercorp.kaelaevent.minigame.games.zombie.player;

import com.covercorp.kaelaevent.minigame.games.zombie.ZombieMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class ZombiePlayer extends MiniGamePlayer<ZombieMiniGame> {
    private int score;

    public ZombiePlayer(ZombieMiniGame miniGame, UUID uniqueId, String name) {
        super(miniGame, uniqueId, name);
    }
}
