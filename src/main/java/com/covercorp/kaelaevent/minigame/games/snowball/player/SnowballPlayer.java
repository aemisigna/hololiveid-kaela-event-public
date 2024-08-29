package com.covercorp.kaelaevent.minigame.games.snowball.player;

import com.covercorp.kaelaevent.minigame.games.snowball.SnowballMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class SnowballPlayer extends MiniGamePlayer<SnowballMiniGame> {
    public SnowballPlayer(final SnowballMiniGame miniGame, final UUID uniqueId, final String name) {
        super(miniGame, uniqueId, name);
    }

    public boolean isDead() {
        Player player = Bukkit.getPlayer(getUniqueId());
        return (player != null && player.getGameMode() == GameMode.SPECTATOR);
    }
}