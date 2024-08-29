package com.covercorp.kaelaevent.minigame.games.squid.arena.task;

import com.covercorp.kaelaevent.minigame.games.squid.arena.SquidArena;
import com.covercorp.kaelaevent.minigame.games.squid.arena.event.SquidPlayerDeathEvent;
import com.covercorp.kaelaevent.minigame.games.squid.arena.event.SquidGalonChanEndAnalyzingEvent;
import com.covercorp.kaelaevent.minigame.games.squid.arena.event.SquidGalonChanStartAnalyzingEvent;
import com.covercorp.kaelaevent.minigame.games.squid.arena.properties.SquidMatchProperties;
import com.covercorp.kaelaevent.minigame.games.squid.arena.state.SquidMatchState;
import com.covercorp.kaelaevent.minigame.games.squid.player.SquidPlayer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class SquidTimeTask implements Runnable {
    private final SquidArena arena;

    private int analysisEndDelay = 3;

    @Override
    public void run() {
        if (arena.getState() != SquidMatchState.GAME) return;
        final SquidMatchProperties properties = arena.getSquidMatchProperties();

        arena.setGameTime(arena.getGameTime() + 1); // Increase game time for this game
        arena.getAnnouncer().sendGlobalSound("kaela:tick", 0.1F, 1.7F);

        if (!properties.isAnalyzing()) {
            properties.setSingingTime(properties.getSingingTime() - 1);
            if (properties.getSingingTime() <= 0) {
                Bukkit.getPluginManager().callEvent(new SquidGalonChanStartAnalyzingEvent(arena));
                properties.setSingingTime(new Random().nextInt(4) + 2);
            }
        }

        if (properties.isAnalyzing()) {
            if (!arena.getMovedTalents().isEmpty()) {
                final UUID uniqueId = arena.getMovedTalents().poll();
                final Optional<SquidPlayer> squidPlayerOptional = arena.getPlayerHelper().getPlayer(uniqueId).map(SquidPlayer.class::cast);
                if (squidPlayerOptional.isEmpty()) return;

                Bukkit.getPluginManager().callEvent(new SquidPlayerDeathEvent(arena, squidPlayerOptional.get()));

                if (arena.getUnfinishedAlivePlayers().size() <= 0) {
                    // properties.isFinalAnalyze()
                    Bukkit.getScheduler().runTaskLater(arena.getSquidMiniGame().getKaelaEvent(), () -> {
                        if (arena.getState() != SquidMatchState.GAME) return;
                        arena.endGame();
                    }, 40L);
                }
                return;
            }

            --analysisEndDelay;
            if (analysisEndDelay <= 0) {
                Bukkit.getPluginManager().callEvent(new SquidGalonChanEndAnalyzingEvent(arena));
                analysisEndDelay = new Random().nextInt(5) + 3;
            }

            return;
        }

        // Check time limit
        final int timeLeft = arena.getTimeLeft();
        if (timeLeft <= 0) {
            arena.endGame();
        }
    }
}
