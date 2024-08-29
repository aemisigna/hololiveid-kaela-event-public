package com.covercorp.kaelaevent.minigame.games.platformrush.arena.task;

import com.covercorp.kaelaevent.minigame.games.platformrush.arena.PlatformRushArena;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.properties.PlatformRushMatchProperties;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.state.PlatformRushMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class PlatformRushStartingTask implements Runnable {
    private final PlatformRushArena arena;

    @Override
    public void run() {
        final PlatformRushMatchProperties props = arena.getPlatformRushMatchProperties();

        arena.setState(PlatformRushMatchState.STARTING);

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
