package com.covercorp.kaelaevent.minigame.games.zombie.arena.listener;

import com.covercorp.kaelaevent.minigame.games.zombie.arena.ZombieArena;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.event.ZombieKillEvent;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.event.ZombieTickEvent;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.spot.zombie.score.ScoreType;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.state.ZombieMatchState;
import com.covercorp.kaelaevent.minigame.games.zombie.player.ZombiePlayer;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class ZombieMatchGameListener implements Listener {
    private final ZombieArena arena;

    public ZombieMatchGameListener(final ZombieArena arena) {
        this.arena = arena;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchTick(final ZombieTickEvent event) {
        if (arena.getState() != ZombieMatchState.GAME) return;

        arena.getPlayerHelper().getPlayerList().forEach(genericPlayer -> {
            final Player player = Bukkit.getPlayer(genericPlayer.getUniqueId());
            if (player == null) return;

            final Location location = player.getLocation();
            if (!arena.getShootZone().containsLocation(location)) player.teleport(arena.getArenaSpawnLocation());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArrowShoot(final ProjectileLaunchEvent event) {
        if (this.arena.getState() != ZombieMatchState.GAME) {
            event.setCancelled(true);
            return;
        }
        final Projectile entity = event.getEntity();
        if (entity instanceof final Arrow arrow) {
            final Vector vector = arrow.getVelocity();
            vector.multiply(0.95);
            arrow.setVelocity(vector);
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onArrowHit(final ProjectileHitEvent event) {
        if (arena.getState() != ZombieMatchState.GAME) return;

        final Projectile projectile = event.getEntity();
        if (!(projectile instanceof Arrow arrow)) return;;

        arrow.remove();

        if (event.getHitEntity() == null) return;

        if (!(arrow.getShooter() instanceof final Player player)) return;
        final Optional<ZombiePlayer> zombiePlayerOptional = arena.getZombieMiniGame().getPlayerHelper().getPlayer(player.getUniqueId()).map(miniGamePlayer -> (ZombiePlayer) miniGamePlayer);
        if (zombiePlayerOptional.isEmpty()) return;

        Bukkit.getPluginManager().callEvent(new ZombieKillEvent(arena, zombiePlayerOptional.get(), event.getHitEntity()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onZombieKill(final ZombieKillEvent event) {
        if (!(event.getEntity() instanceof final Mob entity)) return;
        if (!NBTMetadataUtil.hasEntityString(entity, "zombiespot_id")) return;

        final UUID uuid = entity.getUniqueId();

        entity.remove();

        final ZombiePlayer zombiePlayer = event.getPlayer();
        final Player player = Bukkit.getPlayer(zombiePlayer.getUniqueId());
        if (player == null) return;

        ScoreType scoreType = ScoreType.NORMAL;

        if (NBTMetadataUtil.hasEntityString(entity, "type")) {
            final String type = NBTMetadataUtil.getEntityString(entity, "type");
            if (type.equals("golden")) scoreType = ScoreType.GOLDEN;
            if (type.equals("tnt")) scoreType = ScoreType.TNT;
        }

        switch (scoreType) {
            case NORMAL -> {
                zombiePlayer.setScore(zombiePlayer.getScore() + 1);
                player.showTitle(Title.title(
                        Component.empty(),
                        arena.getGameMiniMessage().deserialize("<green>+1 point"),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(500), Duration.ZERO)
                ));
                player.sendMessage(arena.getGameMiniMessage().deserialize("<green>+1 point"));
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0F, 2.0F);
            }
            case GOLDEN -> {
                zombiePlayer.setScore(zombiePlayer.getScore() + 3);
                player.showTitle(Title.title(
                        Component.empty(),
                        arena.getGameMiniMessage().deserialize("<green>+3 point"),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(500), Duration.ZERO)
                ));
                player.sendMessage(arena.getGameMiniMessage().deserialize("<green>+3 point"));
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0F, 2.0F);
            }
            case TNT -> {
                zombiePlayer.setScore(zombiePlayer.getScore() + 1);
                player.showTitle(Title.title(
                        Component.empty(),
                        arena.getGameMiniMessage().deserialize("<green>+1 point"),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(500), Duration.ZERO)
                ));
                player.sendMessage(arena.getGameMiniMessage().deserialize("<green>+1 point"));
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0F, 2.0F);

                entity.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, entity.getLocation(), 1, 0, 0, 0);
                arena.getAnnouncer().sendGlobalSound(Sound.ENTITY_GENERIC_EXPLODE, 0.7F, 1F);

                entity.getLocation().getWorld().getNearbyEntities(event.getEntity().getLocation(), 4, 4, 4)
                        .stream().filter(explodedEntity -> explodedEntity.getUniqueId() != uuid)
                        .forEach(explodedEntity -> Bukkit.getPluginManager().callEvent(new ZombieKillEvent(arena, zombiePlayer, explodedEntity)));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(final EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;

        event.setDroppedExp(0);
        event.getDrops().clear();
    }
}
