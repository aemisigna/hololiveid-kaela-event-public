package com.covercorp.kaelaevent.minigame.games.zombie.arena.properties;

import com.covercorp.kaelaevent.minigame.games.zombie.arena.ZombieArena;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class ZombieMatchProperties {
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
    private int arenaShuffleTaskId;
    private int fireworkTaskId;

    private int zombieSpawnCooldown;

    public ZombieMatchProperties(final ZombieArena arena) {
        startingTime = 5;
        startingCountdown = startingTime;

        preLobbyTime = 10;
        preLobbyCountdown = preLobbyTime;

        starting = false;

        zombieSpawnCooldown = 5;
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

        setArenaShuffleTaskId(0);

        setZombieSpawnCooldown(5);
    }
}
