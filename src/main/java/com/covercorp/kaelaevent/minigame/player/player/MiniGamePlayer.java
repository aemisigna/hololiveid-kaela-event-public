package com.covercorp.kaelaevent.minigame.player.player;

import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public abstract class MiniGamePlayer<T extends MiniGame> {
    private final T miniGame;

    private final UUID uniqueId;
    private final String name;

    private MiniGameTeam<? extends MiniGamePlayer<T>> miniGameTeam;
}