package com.covercorp.kaelaevent.minigame.games.platformrush.arena.task;

import com.covercorp.kaelaevent.minigame.games.platformrush.arena.PlatformRushArena;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.state.PlatformRushMatchState;
import com.covercorp.kaelaevent.minigame.games.platformrush.inventory.PlatformRushItemCollection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class PlatformRushTimeTask implements Runnable {
    private final PlatformRushArena arena;

    @Override
    public void run() {
        if (arena.getState() != PlatformRushMatchState.GAME) return;

        arena.setGameTime(arena.getGameTime() + 1); // Increase game time for this game

        // Snowball generator
        arena.getPlatformRushMatchProperties().setSnowBallCooldown(
                arena.getPlatformRushMatchProperties().getSnowBallCooldown() + 1
        );

        if (arena.getPlatformRushMatchProperties().getSnowBallCooldown() >= 5) {
            arena.getPlayerHelper().getPlayerList().forEach(genericGamePlayer -> {
                final Player player = Bukkit.getPlayer(genericGamePlayer.getUniqueId());
                if (player == null) return;

                if (!player.getInventory().containsAtLeast(PlatformRushItemCollection.SNOW_BALL, 16)) {
                    player.getInventory().addItem(PlatformRushItemCollection.SNOW_BALL);
                }

                arena.getPlatformRushMatchProperties().setSnowBallCooldown(0);
            });
        }
    }
}
