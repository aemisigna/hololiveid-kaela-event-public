package com.covercorp.kaelaevent.minigame.games.zombie.arena.task;

import com.covercorp.kaelaevent.minigame.games.zombie.arena.ZombieArena;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.properties.ZombieMatchProperties;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.state.ZombieMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ZombieStartingTask implements Runnable {
    private final ZombieArena arena;

    @Override
    public void run() {
        final ZombieMatchProperties props = arena.getZombieMatchProperties();

        arena.setState(ZombieMatchState.STARTING);

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
