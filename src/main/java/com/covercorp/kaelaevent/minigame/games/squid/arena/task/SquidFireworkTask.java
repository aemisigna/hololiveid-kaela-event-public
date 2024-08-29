package com.covercorp.kaelaevent.minigame.games.squid.arena.task;

import com.covercorp.kaelaevent.minigame.games.squid.arena.SquidArena;
import com.covercorp.kaelaevent.minigame.games.squid.arena.state.SquidMatchState;
import com.covercorp.kaelaevent.minigame.games.squid.team.SquidTeam;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class SquidFireworkTask implements Runnable {
    private final SquidArena arena;
    private final SquidTeam winnerTeam;

    @Override
    public void run() {
        if (arena.getState() != SquidMatchState.ENDING) {
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
