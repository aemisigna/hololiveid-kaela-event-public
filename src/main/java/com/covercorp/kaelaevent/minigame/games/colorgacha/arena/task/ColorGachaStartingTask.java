package com.covercorp.kaelaevent.minigame.games.colorgacha.arena.task;

import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.ColorGachaArena;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.properties.ColorGachaMatchProperties;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.state.ColorGachaMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ColorGachaStartingTask implements Runnable {
    private final ColorGachaArena arena;

    @Override
    public void run() {
        final ColorGachaMatchProperties props = arena.getColorGachaMatchProperties();

        arena.setState(ColorGachaMatchState.STARTING);

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
