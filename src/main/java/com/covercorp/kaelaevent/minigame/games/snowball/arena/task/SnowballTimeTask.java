package com.covercorp.kaelaevent.minigame.games.snowball.arena.task;

import com.covercorp.kaelaevent.minigame.games.snowball.arena.SnowballArena;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.event.SnowballTntEvent;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.state.SnowballMatchState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class SnowballTimeTask implements Runnable {
    private final SnowballArena arena;

    public void run() {
        if (arena.getState() != SnowballMatchState.GAME) return;

        arena.setGameTime(arena.getGameTime() + 1);
        arena.getAnnouncer().sendGlobalSound("kaela:tick", 0.1F, 1.7F);

        if (arena.getSnowballMatchProperties().getTntCooldown() <= 3) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getEquipment().getHelmet() == null) return;

                if (player.getEquipment().getHelmet().getType() == Material.TNT) {
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, 0.7F);
                }
            });
        }

        // TNT cooldown
        arena.getSnowballMatchProperties().setTntCooldown(arena.getSnowballMatchProperties().getTntCooldown() - 1);
        if (arena.getSnowballMatchProperties().getTntCooldown() <= 0) {
            arena.getSnowballMatchProperties().setTntCooldown(15);

            Bukkit.getPluginManager().callEvent(new SnowballTntEvent(arena));
        }
    }
}