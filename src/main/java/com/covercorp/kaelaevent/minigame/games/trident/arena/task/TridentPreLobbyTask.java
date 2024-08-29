package com.covercorp.kaelaevent.minigame.games.trident.arena.task;

import com.covercorp.kaelaevent.minigame.games.trident.arena.TridentArena;
import com.covercorp.kaelaevent.minigame.games.trident.arena.properties.TridentMatchProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TridentPreLobbyTask implements Runnable {
    private final TridentArena arena;

    @Override
    public void run() {
        final TridentMatchProperties props = arena.getTridentMatchProperties();

        props.setPreLobby(true);

        if (props.getPreLobbyCountdown() >= 10) {
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                    "<gray>[!] Tridents will appear in a moment... get prepared!"
            ));
            arena.getAnnouncer().sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, 0.7F);
        }
        if (props.getPreLobbyCountdown() <= 5 && props.getPreLobbyCountdown() > 0) {
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                    "<yellow>Tridents will be enabled in <aqua>" + props.getPreLobbyCountdown() + " second(s)<yellow>."
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
