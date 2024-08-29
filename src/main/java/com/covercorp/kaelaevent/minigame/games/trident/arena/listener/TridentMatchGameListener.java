package com.covercorp.kaelaevent.minigame.games.trident.arena.listener;

import com.covercorp.kaelaevent.minigame.games.trident.arena.TridentArena;
import com.covercorp.kaelaevent.minigame.games.trident.arena.event.TridentShotEvent;
import com.covercorp.kaelaevent.minigame.games.trident.arena.event.TridentTickEvent;
import com.covercorp.kaelaevent.minigame.games.trident.arena.state.TridentMatchState;
import com.covercorp.kaelaevent.minigame.games.trident.inventory.TridentItemCollection;
import com.covercorp.kaelaevent.minigame.games.trident.player.TridentPlayer;
import com.covercorp.kaelaevent.minigame.games.trident.team.TridentTeam;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.BoundingBox;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class TridentMatchGameListener implements Listener {
    private final TridentArena arena;
    private final Cache<Trident, Boolean> tridentCache;

    public TridentMatchGameListener(final TridentArena arena) {
        this.arena = arena;

        this.tridentCache = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.SECONDS).build();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchTick(final TridentTickEvent event) {
        if (arena.getState() != TridentMatchState.GAME) return;

        arena.getTargets().forEach((uuid, targetTrident) -> {
            if (targetTrident.getHitbox() == null) return;
            if (targetTrident.getDisplay() == null) return;
            if (targetTrident.getHitbox().isDead() || targetTrident.getDisplay().isDead()) return;

            final BoundingBox boundingBox = targetTrident.getHitbox().getBoundingBox();

            tridentCache.asMap().keySet().forEach(trident -> {
                if (!((trident.getShooter()) instanceof final Player player)) return;

                if (!boundingBox.contains(trident.getLocation().getX(), trident.getLocation().getY(), trident.getLocation().getZ())) return;

                final Optional<TridentPlayer> tridentPlayerOptional = arena.getPlayerHelper()
                        .getPlayer(player.getUniqueId())
                        .map(generic -> (TridentPlayer)generic);
                if (tridentPlayerOptional.isEmpty()) return;

                final TridentPlayer tridentPlayer = tridentPlayerOptional.get();
                final TridentTeam team = (TridentTeam) tridentPlayer.getMiniGameTeam();
                if (team == null) return;

                tridentCache.invalidate(trident);
                trident.remove();
                targetTrident.deSpawn();

                arena.getArenaCenter().getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, targetTrident.getLocation(), 1, 0.0, 0.0, 0.0);
                arena.getArenaCenter().getWorld().playSound(targetTrident.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3.0F, 0.7F);

                Bukkit.getPluginManager().callEvent(new TridentShotEvent(arena, tridentPlayer));
            });
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTridentShot(final TridentShotEvent event) {
        if (arena.getState() != TridentMatchState.GAME) return;

        final TridentPlayer tridentPlayer = event.getTridentPlayer();
        final Player player = Bukkit.getPlayer(tridentPlayer.getUniqueId());
        if (player == null) return;

        tridentPlayer.setScore(tridentPlayer.getScore() + 1);

        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.5F, 1.5F);
        player.showTitle(
                Title.title(
                        Component.empty(),
                        arena.getGameMiniMessage().deserialize("<green>+1 point"),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(500L), Duration.ZERO)
                )
        );

        if (tridentPlayer.getScore() >= 20) {
            arena.checkWinner();
            return;
        }

        arena.spawnTarget();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTridentGround(final ProjectileHitEvent event) {
        final Projectile projectile = event.getEntity();
        if (!(projectile instanceof Trident trident)) return;
        if (event.getHitBlock() == null) return;

        trident.remove();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTridentLaunch(final ProjectileLaunchEvent event) {
        if (this.arena.getState() != TridentMatchState.GAME) {
            event.setCancelled(true);
            return;
        }

        final Projectile entity = event.getEntity();

        if (!(entity instanceof Trident trident)) return;

        if (!(trident.getShooter() instanceof final Player player)) return;

        player.getInventory().setItem(0, TridentItemCollection.TRIDENT_ITEM);
        tridentCache.put(trident, true);
    }
}
