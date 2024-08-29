package com.covercorp.kaelaevent.minigame.games.squid.arena.properties;

import com.covercorp.kaelaevent.minigame.games.squid.arena.SquidArena;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class SquidMatchProperties {
    // Used por start items
    private int startingTime;
    private int startingCountdown;
    private boolean starting;

    // Used for the time before talents can break snow blocks
    private int preLobbyTime;
    private int preLobbyCountdown;
    private boolean preLobby;

    private int preLobbyTaskId;
    private int startingTaskId;

    private int arenaTickTaskId;
    private int arenaTimeTaskId;
    private int fireworkTaskId;

    private Instant startTime;
    private int singingTime = 3;
    private boolean analyzing;
    private boolean finalAnalyze;

    public SquidMatchProperties(final SquidArena arena) {
        startingTime = 5;
        startingCountdown = startingTime;

        preLobbyTime = 10;
        preLobbyCountdown = preLobbyTime;

        starting = false;
    }

    public void resetTimer() {
        setStartingTime(5);
        setStarting(false);
        setStartingTaskId(0);
        setStartingCountdown(startingTime);

        setPreLobbyTime(10);
        setPreLobby(false);
        setPreLobbyTaskId(0);
        setPreLobbyCountdown(preLobbyTime);

        setStartTime(Instant.now());
        setSingingTime(5);
        setAnalyzing(false);
        setFinalAnalyze(false);
    }
}
