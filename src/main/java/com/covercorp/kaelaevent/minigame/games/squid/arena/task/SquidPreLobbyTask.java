package com.covercorp.kaelaevent.minigame.games.squid.arena.task;

import com.covercorp.kaelaevent.minigame.games.squid.arena.SquidArena;
import com.covercorp.kaelaevent.minigame.games.squid.arena.properties.SquidMatchProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class SquidPreLobbyTask implements Runnable {
    private final SquidArena arena;

    @Override
    public void run() {
        final SquidMatchProperties props = arena.getSquidMatchProperties();

        props.setPreLobby(true);

        if (props.getPreLobbyCountdown() >= 10) {
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                    "<gray>[!] Galon-chan's game will start in a moment... get prepared!"
            ));
            arena.getAnnouncer().sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, 0.7F);
        }
        if (props.getPreLobbyCountdown() <= 5 && props.getPreLobbyCountdown() > 0) {
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                    "<yellow>Galon-chan's game will start in <aqua>" + props.getPreLobbyCountdown() + " second(s)<yellow>."
            ));
            arena.getAnnouncer().sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
        }
        if (props.getPreLobbyCountdown() == 0) {
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                    "<gray>[!] Starting match..."
            ));

            arena.postStart();

            return;
        }

        props.setPreLobbyCountdown(props.getPreLobbyCountdown() - 1);
    }
}
