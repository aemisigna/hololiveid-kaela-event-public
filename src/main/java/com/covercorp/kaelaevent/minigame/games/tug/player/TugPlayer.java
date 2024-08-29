package com.covercorp.kaelaevent.minigame.games.tug.player;

import com.covercorp.kaelaevent.minigame.games.tug.TugMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public final class TugPlayer extends MiniGamePlayer<TugMiniGame> {
    private int score;

    public TugPlayer(final TugMiniGame miniGame, final UUID uniqueId, final String name) {
        super(miniGame, uniqueId, name);
    }

    public boolean isSpectating() {
        final Player player = Bukkit.getPlayer(getUniqueId());
        if (player == null) return false;

        return player.getGameMode() == GameMode.SPECTATOR;
    }
}
