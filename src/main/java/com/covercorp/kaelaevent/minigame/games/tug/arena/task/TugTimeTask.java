package com.covercorp.kaelaevent.minigame.games.tug.arena.task;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.tug.TugMiniGame;
import com.covercorp.kaelaevent.minigame.games.tug.arena.TugArena;
import com.covercorp.kaelaevent.minigame.games.tug.arena.state.TugMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;

import java.time.Duration;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TugTimeTask implements Runnable {
    private final TugArena arena;

    private static final int[] CRITICAL_TIMES = {60, 30, 15, 5, 4, 3, 2, 1};

    @Override
    public void run() {
        if (arena.getState() != TugMatchState.GAME) return;

        arena.setGameTime(arena.getGameTime() + 1); // Increase game time for this game

        // Check time limit
        final Announcer<TugMiniGame> announcer = arena.getAnnouncer();
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

        // The game should finish
        if (timeLeft <= 0) {
            arena.runLoser();
        }
    }
}
