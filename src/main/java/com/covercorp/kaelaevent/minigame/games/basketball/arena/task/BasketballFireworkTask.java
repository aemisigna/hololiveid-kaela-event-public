package com.covercorp.kaelaevent.minigame.games.basketball.arena.task;

import com.covercorp.kaelaevent.minigame.games.basketball.arena.BasketballArena;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.state.BasketballMatchState;
import com.covercorp.kaelaevent.minigame.games.basketball.team.BasketballTeam;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BasketballFireworkTask implements Runnable {
    private final BasketballArena arena;
    private final BasketballTeam winnerTeam;

    @Override
    public void run() {
        if (arena.getState() != BasketballMatchState.ENDING) {
            return;
        }

        winnerTeam.getPlayers().stream().toList().forEach(winner -> {
            final Player player = Bukkit.getPlayer(winner.getUniqueId());
            if (player == null) return;

            final World world = player.getWorld();

            final Firework firework = world.spawn(player.getLocation(), Firework.class);
            final FireworkMeta fireworkMeta = firework.getFireworkMeta();

            fireworkMeta.setPower(2);
            fireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.GREEN).withFade(Color.YELLOW).build());

            firework.setFireworkMeta(fireworkMeta);
            firework.detonate();

            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8F, 0.8F);
            player.spawnParticle(Particle.FIREWORK, player.getLocation(), 100, 0.5, 0.5, 0.5, 0.1);
        });
    }
}
