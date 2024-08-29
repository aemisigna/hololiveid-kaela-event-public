package com.covercorp.kaelaevent.minigame.games.basketball.arena.task;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.basketball.BasketballMiniGame;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.BasketballArena;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.state.BasketballMatchState;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;

import java.time.Duration;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BasketballTimeTask implements Runnable {
    private final BasketballArena arena;

    private static final int[] CRITICAL_TIMES = {60, 30, 15, 5, 4, 3, 2, 1};

    @Override
    public void run() {
        if (arena.getState() != BasketballMatchState.GAME) return;

        arena.setGameTime(arena.getGameTime() + 1); // Increase game time for this game

        // Check time limit
        final Announcer<BasketballMiniGame> announcer = arena.getAnnouncer();
        final int timeLeft = arena.getTimeLeft();

        // Check times for announcements
        for (int criticalTime : CRITICAL_TIMES) {
            if (timeLeft == criticalTime) {
                announcer.sendGlobalSound(Sound.UI_BUTTON_CLICK, 2.0F, 2.0F);
                announcer.sendGlobalMessage("&eThe game will end in &b" + criticalTime + " seconds&e.", false);
                announcer.sendGlobalTitle(Title.title(
                        Component.empty(),
                        arena.getGameMiniMessage().deserialize("<aqua>" + criticalTime + " seconds <yellow>left!"),
                        Title.Times.times(Duration.ofMillis(0), Duration.ofSeconds(2), Duration.ofMillis(0))
                ));
                break;
            }
        }

        arena.getBasketballMatchProperties().setBallSpawnCooldown(arena.getBasketballMatchProperties().getBallSpawnCooldown() - 1);
        // Tick spawners
        if (arena.getBasketballMatchProperties().getBallSpawnCooldown() <= 0) {
            arena.getAnnouncer().sendGlobalMessage("&eMore basketballs are spawning in the playfield!", false);
            arena.getAnnouncer().sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
            arena.spawnBalls();

            arena.getBasketballMatchProperties().setBallSpawnCooldown(6);
        }

        // The game should finish
        if (timeLeft <= 0) {
            arena.checkWinner();
        }
    }
}
