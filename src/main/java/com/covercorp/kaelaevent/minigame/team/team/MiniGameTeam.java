package com.covercorp.kaelaevent.minigame.team.team;

import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import net.kyori.adventure.text.Component;
import org.eclipse.sisu.Nullable;

import java.util.HashSet;
import java.util.Set;

public abstract class MiniGameTeam<T extends MiniGamePlayer<? extends MiniGame>> {
    @Getter(AccessLevel.PUBLIC) private final String identifier;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private Component prefix;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private @Nullable Component betterPrefix;

    @Getter(AccessLevel.PUBLIC) private final Set<T> players;

    public MiniGameTeam(final String identifier) {
        this.identifier = identifier;

        this.players = new HashSet<>();
    }

    public void addPlayer(final T player) {
        players.add(player);
    }

    public void removePlayer(final T player) {
        players.remove(player);
    }
}
