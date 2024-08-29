package com.covercorp.kaelaevent.minigame.games.colorgacha.player;

import com.covercorp.kaelaevent.minigame.games.colorgacha.ColorGachaMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ColorGachaPlayer extends MiniGamePlayer<ColorGachaMiniGame> {
    public ColorGachaPlayer(final ColorGachaMiniGame miniGame, final UUID uniqueId, final String name) {
        super(miniGame, uniqueId, name);
    }

    public boolean isDead() {
        final Player player = Bukkit.getPlayer(getUniqueId());
        return player != null && player.getGameMode()  == GameMode.SPECTATOR;
    }
}
