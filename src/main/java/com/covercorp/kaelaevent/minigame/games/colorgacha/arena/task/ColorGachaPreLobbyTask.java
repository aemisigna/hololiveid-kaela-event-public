package com.covercorp.kaelaevent.minigame.games.colorgacha.arena.task;

import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.ColorGachaArena;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.properties.ColorGachaMatchProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ColorGachaPreLobbyTask implements Runnable {
    private final ColorGachaArena arena;

    @Override
    public void run() {
        final ColorGachaMatchProperties props = arena.getColorGachaMatchProperties();

        props.setPreLobby(true);

        if (props.getPreLobbyCountdown() >= 10) {
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                    "<gray>[!] Kaela's button machines will be enabled in a moment... get prepared!"
            ));
            arena.getAnnouncer().sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, 0.7F);
        }
        if (props.getPreLobbyCountdown() <= 5 && props.getPreLobbyCountdown() > 0) {
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                    "<yellow>Kaela's button machines will be enabled in a moment <aqua>" + props.getPreLobbyCountdown() + " second(s)<yellow>."
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
