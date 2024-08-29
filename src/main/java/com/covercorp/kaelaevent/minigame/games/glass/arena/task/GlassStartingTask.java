package com.covercorp.kaelaevent.minigame.games.glass.arena.task;

import com.covercorp.kaelaevent.minigame.games.glass.arena.GlassArena;
import com.covercorp.kaelaevent.minigame.games.glass.arena.properties.GlassMatchProperties;
import com.covercorp.kaelaevent.minigame.games.glass.arena.state.GlassMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class GlassStartingTask implements Runnable {
    private final GlassArena arena;

    @Override
    public void run() {
        final GlassMatchProperties props = arena.getGlassMatchProperties();

        arena.setState(GlassMatchState.STARTING);

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
