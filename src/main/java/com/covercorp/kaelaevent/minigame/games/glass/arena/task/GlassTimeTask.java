package com.covercorp.kaelaevent.minigame.games.glass.arena.task;

import com.covercorp.kaelaevent.minigame.games.glass.arena.GlassArena;
import com.covercorp.kaelaevent.minigame.games.glass.arena.state.GlassMatchState;
import java.time.Duration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.Sound;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class GlassTimeTask implements Runnable {
    private final GlassArena arena;

    private static final int[] CRITICAL_TIMES = new int[]{60, 30, 15, 5, 4, 3, 2, 1};

    public void run() {
        if (arena.getState() != GlassMatchState.GAME) return;

        arena.setGameTime(this.arena.getGameTime() + 1);
        arena.getAnnouncer().sendGlobalSound("kaela:tick", 0.1F, 1.7F);

        int timeLeft = this.arena.getTimeLeft();

        for (int criticalTime : CRITICAL_TIMES) {
            if (timeLeft == criticalTime) {
                arena.getAnnouncer().sendGlobalSound(Sound.UI_BUTTON_CLICK, 2.0F, 2.0F);
                arena.getAnnouncer().sendGlobalMessage("&eThe bridge will explode in &b" + criticalTime + " seconds&e.", false);
                arena.getAnnouncer().sendGlobalTitle(
                        Title.title(
                                Component.empty(),
                                this.arena.getGameMiniMessage().deserialize("<aqua>" + criticalTime + " seconds <yellow>left!"),
                                Times.times(Duration.ofMillis(0L), Duration.ofSeconds(2L), Duration.ofMillis(0L))
                        )
                );
                break;
            }
        }

        if (timeLeft <= 0) {
            this.arena.breakBridge();
        }
    }
}