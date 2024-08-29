package com.covercorp.kaelaevent.minigame.games.target.arena.task;

import com.covercorp.kaelaevent.minigame.games.target.arena.TargetArena;
import com.covercorp.kaelaevent.minigame.games.target.arena.properties.TargetMatchProperties;
import com.covercorp.kaelaevent.minigame.games.target.arena.state.TargetMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TargetStartingTask implements Runnable {
    private final TargetArena arena;

    @Override
    public void run() {
        final TargetMatchProperties props = arena.getTargetMatchProperties();

        arena.setState(TargetMatchState.STARTING);

        props.setStarting(true);

        if (props.getStartingCountdown() >= 1) {
            if (props.getStartingCountdown() == 5) arena.getAnnouncer().sendGlobalSound(Sound.ITEM_GOAT_HORN_SOUND_2, 0.8F, 0.8F);
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                    "<gray>[!] The game will start in " + props.getStartingCountdown() + " seconds, get prepared!"
            ));
        }
        if (props.getStartingCountdown() == 0) {
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                    "<gray>[!] Starting match..."
            ));
            arena.start();
            return;
        }

        props.setStartingCountdown(props.getStartingCountdown() - 1);
    }
}
