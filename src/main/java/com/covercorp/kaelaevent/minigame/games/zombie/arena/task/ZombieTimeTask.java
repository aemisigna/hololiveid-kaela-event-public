package com.covercorp.kaelaevent.minigame.games.zombie.arena.task;

import com.covercorp.kaelaevent.minigame.games.zombie.arena.ZombieArena;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.state.ZombieMatchState;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;

import java.time.Duration;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ZombieTimeTask implements Runnable {
    private final ZombieArena arena;

    private static final int[] CRITICAL_TIMES = {60, 30, 15, 5, 4, 3, 2, 1};

    @Override
    public void run() {
        if (arena.getState() != ZombieMatchState.GAME) return;

        final int timeLeft = arena.getTimeLeft();

        arena.setGameTime(arena.getGameTime() + 1);
        arena.getAnnouncer().sendGlobalSound("kaela:tick", 0.1F, 1.7F);

        for (int criticalTime : CRITICAL_TIMES) {
            if (timeLeft == criticalTime) {
                arena.getAnnouncer().sendGlobalSound(Sound.UI_BUTTON_CLICK, 2.0F, 2.0F);
                arena.getAnnouncer().sendGlobalMessage("&eThe game will end in &b" + criticalTime + " seconds&e.", false);
                arena.getAnnouncer().sendGlobalTitle(Title.title(
                        Component.empty(),
                        arena.getGameMiniMessage().deserialize("<aqua>" + criticalTime + " seconds <yellow>left!"),
                        Title.Times.times(Duration.ofMillis(0), Duration.ofSeconds(2), Duration.ofMillis(0))
                ));
                break;
            }
        }

        if (timeLeft <= 0) {
            arena.checkWinner();
            return;
        }

        // Zombie spawn task
        arena.getZombieMatchProperties().setZombieSpawnCooldown(arena.getZombieMatchProperties().getZombieSpawnCooldown() - 1);
        if (arena.getZombieMatchProperties().getZombieSpawnCooldown() <= 0) {
            arena.spawnZombies();

            arena.getZombieMatchProperties().setZombieSpawnCooldown(5);
        }
    }
}
