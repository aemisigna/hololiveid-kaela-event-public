package com.covercorp.kaelaevent.minigame.games.target.arena.task;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.target.TargetMiniGame;
import com.covercorp.kaelaevent.minigame.games.target.arena.TargetArena;
import com.covercorp.kaelaevent.minigame.games.target.arena.state.TargetMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;

import java.time.Duration;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TargetTimeTask implements Runnable {
    private final TargetArena arena;

    private static final int[] CRITICAL_TIMES = {60, 30, 15, 5, 4, 3, 2, 1};

    @Override
    public void run() {
        if (arena.getState() != TargetMatchState.GAME) return;

        arena.setGameTime(arena.getGameTime() + 1); // Increase game time for this game
        arena.getAnnouncer().sendGlobalSound("kaela:tick", 0.1F, 1.7F);

        // Check time limit
        final Announcer<TargetMiniGame> announcer = arena.getAnnouncer();
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

        this.arena.getTargetMatchProperties().setTargetSpawnCooldown(this.arena.getTargetMatchProperties().getTargetSpawnCooldown() - 1);
        if (this.arena.getTargetMatchProperties().getTargetSpawnCooldown() <= 0) {
            this.arena.spawnTargets(arena.getTargets().size() - 4);
            this.arena.getTargetMatchProperties().setTargetSpawnCooldown(5);
        }

        // The game should finish
        if (timeLeft <= 0) {
            arena.setWinner();
        }
    }
}
