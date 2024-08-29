package com.covercorp.kaelaevent.minigame.games.tug.arena.task;

import com.covercorp.kaelaevent.minigame.games.tug.arena.TugArena;
import com.covercorp.kaelaevent.minigame.games.tug.arena.state.TugMatchState;
import com.covercorp.kaelaevent.minigame.games.tug.team.TugTeam;
import com.covercorp.kaelaevent.minigame.games.tug.util.TugMatchUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TugLoserKillTask implements Runnable {
    private final TugArena arena;
    private final TugTeam loserTeam;

    @Override
    public void run() {
        if (arena.getState() != TugMatchState.ENDING) return;

        final Location center = arena.getCenterLocation();
        final World centerWorld = center.getWorld();
        if (centerWorld == null) return;

        arena.getAnnouncer().sendGlobalSound(Sound.ENTITY_PLAYER_BIG_FALL, 1.0F, 1.2F);

        loserTeam.getPlayers().stream().toList().forEach(winner -> {
            final Player player = Bukkit.getPlayer(winner.getUniqueId());
            if (player == null) return;

            Bukkit.getScheduler().runTaskLater(arena.getTugMiniGame().getKaelaEvent(), () -> {
                Bukkit.getOnlinePlayers().forEach(oPlayer -> oPlayer.playSound(oPlayer, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 0.8F));

                if (player.getGameMode() != GameMode.SPECTATOR) {
                    //player.teleport(player.getLocation().add(0, 4.5, 0));
                    // lift the player up
                    player.setVelocity(new Vector(0.0, 4.5, 0.0));

                    Bukkit.getScheduler().runTaskLater(arena.getTugMiniGame().getKaelaEvent(), () -> {
                        Location playerLocation = player.getLocation();
                        Vector direction = center.toVector().subtract(playerLocation.toVector()).normalize();

                        // Push them towards the center
                        player.setVelocity(direction.multiply(16.0));

                        //TugMatchUtil.fuckingKillThemAlready(player, center);
                    },8L);
                }
            }, 15L);
        });

        Bukkit.getScheduler().runTaskLater(arena.getTugMiniGame().getKaelaEvent(), arena::stop, 60L);
    }
}
