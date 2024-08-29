package com.covercorp.kaelaevent.minigame.games.target.arena.listener;

import com.covercorp.kaelaevent.minigame.games.target.arena.TargetArena;
import com.covercorp.kaelaevent.minigame.games.target.arena.event.TargetTickEvent;
import com.covercorp.kaelaevent.minigame.games.target.arena.state.TargetMatchState;
import com.covercorp.kaelaevent.minigame.games.target.inventory.TargetItemCollection;
import com.covercorp.kaelaevent.minigame.games.target.player.TargetPlayer;
import com.covercorp.kaelaevent.minigame.games.target.team.TargetTeam;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import com.covercorp.kaelaevent.util.PlayerUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class TargetMatchGameListener implements Listener {
    private final TargetArena arena;
    private final Cache<Arrow, Boolean> arrowCache;
    private final Cache<UUID, Instant> rechargeCache;

    public TargetMatchGameListener(final TargetArena arena) {
        this.arena = arena;

        this.arrowCache = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.SECONDS).build();
        this.rechargeCache = CacheBuilder.newBuilder().expireAfterWrite(6L, TimeUnit.SECONDS).build();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchTick(final TargetTickEvent event) {
        if (arena.getState() != TargetMatchState.GAME) return;

        arena.getTargets().forEach((uuid, target) -> {
            if (target.getHitbox() == null) return;
            if (target.getDisplay() == null) return;
            if (target.getHitbox().isDead() || target.getDisplay().isDead()) return;

            final BoundingBox boundingBox = target.getHitbox().getBoundingBox();

            arrowCache.asMap().keySet().forEach(arrow -> {
                if (!((arrow.getShooter()) instanceof final Player player)) return;

                if (!boundingBox.contains(arrow.getLocation().getX(), arrow.getLocation().getY(), arrow.getLocation().getZ())) return;

                final Optional<TargetPlayer> targetPlayerOptional = arena.getPlayerHelper()
                        .getPlayer(player.getUniqueId())
                        .map(generic -> (TargetPlayer)generic);
                if (targetPlayerOptional.isEmpty()) return;

                TargetPlayer targetPlayer = targetPlayerOptional.get();
                TargetTeam team = (TargetTeam) targetPlayer.getMiniGameTeam();
                if (team == null) return;

                targetPlayer.setScore(targetPlayer.getScore() + 1);

                arrowCache.invalidate(arrow);
                arrow.remove();

                arena.getArenaCenter().getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, target.getLocation(), 1, 0.0, 0.0, 0.0);
                arena.getArenaCenter().getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3.0F, 0.7F);

                target.deSpawn();

                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.5F, 1.5F);
                player.showTitle(
                        Title.title(
                                Component.empty(),
                                this.arena.getGameMiniMessage().deserialize("<green>+1 point"),
                                Title.Times.times(Duration.ZERO, Duration.ofMillis(800L), Duration.ZERO)
                        )
                );
            });
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArrowRecharge(final PlayerInteractAtEntityEvent event) {
        if (arena.getState() != TargetMatchState.GAME) return;

        final Player player = event.getPlayer();
        if (event.getRightClicked().getType() != EntityType.INTERACTION) return;
        if (!NBTMetadataUtil.hasEntityString(event.getRightClicked(), "arrow_gen")) return;

        final Instant lastRecharge = rechargeCache.getIfPresent(player.getUniqueId());
        if (lastRecharge != null) {
            final Duration elapsed = Duration.between(lastRecharge, Instant.now());
            final long timeLeftMillis = 6000L - elapsed.toMillis();
            if (timeLeftMillis > 0L) {
                double timeLeftSeconds = timeLeftMillis / 1000.0;
                timeLeftSeconds = Math.round(timeLeftSeconds * 10.0) / 10.0;
                player.showTitle(Title.title(
                        Component.empty(),
                        arena.getGameMiniMessage().deserialize(String.format("<red>Wait <yellow>%.1f <red>seconds to recharge again!", timeLeftSeconds)),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(1000L), Duration.ZERO)));
                return;
            }
        }
        final Inventory inventory = player.getInventory();
        if (inventory.containsAtLeast(TargetItemCollection.ARROW, 3)) {
            player.showTitle(Title.title(Component.empty(), this.arena.getGameMiniMessage().deserialize("<red>You can't carry more than 3 arrows! Go shoot!"), Title.Times.times(Duration.ZERO, Duration.ofMillis(1000L), Duration.ZERO)));
            return;
        }
        inventory.setItem(1, new ItemBuilder(TargetItemCollection.ARROW).withAmount(3).build());
        player.playSound(player, Sound.BLOCK_PISTON_EXTEND, 1.2f, 1.2f);
        player.showTitle(Title.title(Component.empty(), this.arena.getGameMiniMessage().deserialize("<green>Recharge complete! Go shoot!"), Title.Times.times(Duration.ZERO, Duration.ofMillis(1000L), Duration.ZERO)));

        PlayerUtils.forceSwingAnimation(player);

        rechargeCache.put(player.getUniqueId(), Instant.now());
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onArrowHit(final ProjectileHitEvent event) {
        final Projectile projectile = event.getEntity();
        if (!(projectile instanceof Arrow arrow)) return;
        if (event.getHitBlock() == null) return;

        arrow.remove();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArrowShoot(final ProjectileLaunchEvent event) {
        if (this.arena.getState() != TargetMatchState.GAME) {
            return;
        }
        final Projectile entity = event.getEntity();
        if (entity instanceof final Arrow arrow) {
            final Vector vector = arrow.getVelocity();
            vector.multiply(0.7);
            arrow.setVelocity(vector);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArrow(final ProjectileLaunchEvent event) {
        if (this.arena.getState() != TargetMatchState.GAME) return;

        final Projectile entity = event.getEntity();

        if (!(entity instanceof Arrow arrow)) return;

        if (!(arrow.getShooter() instanceof Player)) return;

        this.arrowCache.put(arrow, true);
    }
}
