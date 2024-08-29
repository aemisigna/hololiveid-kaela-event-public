package com.covercorp.kaelaevent.minigame.games.glass.player;

import com.covercorp.kaelaevent.minigame.games.glass.GlassMiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public final class GlassPlayer extends MiniGamePlayer<GlassMiniGame> {
    public GlassPlayer(GlassMiniGame miniGame, UUID uniqueId, String name) {
        super(miniGame, uniqueId, name);
    }

    public boolean isDead() {
        Player player = Bukkit.getPlayer(getUniqueId());
        return (player != null && player.getGameMode() == GameMode.SPECTATOR);
    }
}