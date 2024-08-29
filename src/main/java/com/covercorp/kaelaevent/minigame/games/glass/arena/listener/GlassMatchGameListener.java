package com.covercorp.kaelaevent.minigame.games.glass.arena.listener;

import com.covercorp.kaelaevent.minigame.games.glass.arena.*;
import com.covercorp.kaelaevent.minigame.games.glass.arena.state.*;
import org.bukkit.event.entity.*;
import com.covercorp.kaelaevent.minigame.games.glass.player.*;
import com.covercorp.kaelaevent.minigame.games.glass.arena.event.*;
import org.bukkit.event.*;
import org.bukkit.entity.*;
import java.util.*;
import org.bukkit.*;
import com.covercorp.kaelaevent.minigame.games.glass.arena.glass.side.*;
import org.bukkit.util.*;

public final class GlassMatchGameListener implements Listener {
    private final GlassArena arena;

    public GlassMatchGameListener(final GlassArena arena) {
        this.arena = arena;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchTick(final GlassTickEvent event) {
        if (this.arena.getState() != GlassMatchState.GAME) {
            return;
        }
        this.arena.getBridgeParts().forEach((uuid, bridgePart) -> {
            if (this.arena.getState() != GlassMatchState.GAME) {
                return;
            }
            bridgePart.getGlasses().stream().filter(GlassSide::isAlive).filter(GlassSide::isEvil).forEach(glass -> this.arena.getPlayerHelper().getPlayerList().stream().map(generic -> (GlassPlayer)generic).filter(glassPlayer -> !glassPlayer.isDead()).forEach(glassPlayer -> {
                final Player player = Bukkit.getPlayer(glassPlayer.getUniqueId());
                if (player == null) {
                    return;
                }
                final Location location = player.getLocation();
                if (glass.getHitbox() == null) {
                    return;
                }
                if (glass.getDisplay() == null) {
                    return;
                }
                final BoundingBox boundingBox = glass.getHitbox().getBoundingBox();
                if (boundingBox.contains(location.getX(), location.getY(), location.getZ())) {
                    glass.getCenter().getWorld().playSound(glass.getCenter(), "kaela:glassbreak", 0.3f, 0.8f);
                    bridgePart.breakGlass(glass);
                }
            }));
        });
        this.arena.getPlayerHelper().getPlayerList().stream().map(generic -> (GlassPlayer)generic).filter(glassPlayer -> !glassPlayer.isDead()).forEach(glassPlayer -> {
            final Player player = Bukkit.getPlayer(glassPlayer.getUniqueId());
            if (player == null) {
                return;
            }
            final Location location = player.getLocation();
            if (!this.arena.getFinishZone().containsLocation(location)) {
                return;
            }
            Bukkit.getPluginManager().callEvent(new GlassFinishEvent(this.arena, glassPlayer));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFinish(final GlassFinishEvent event) {
        if (this.arena.getState() != GlassMatchState.GAME) {
            return;
        }
        this.arena.stop();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFallDamage(final EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }
        event.setCancelled(true);
        if (this.arena.getState() != GlassMatchState.GAME) {
            return;
        }
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        final Optional<GlassPlayer> gamePlayerOptional = this.arena.getPlayerHelper().getPlayer(player.getUniqueId()).map(genericPlayer -> (GlassPlayer)genericPlayer);
        if (gamePlayerOptional.isEmpty()) {
            return;
        }
        Bukkit.getPluginManager().callEvent(new GlassDisqualificationEvent(this.arena, gamePlayerOptional.get()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisqualification(final GlassDisqualificationEvent event) {
        if (this.arena.getState() != GlassMatchState.GAME) {
            return;
        }
        final GlassPlayer glassPlayer = event.getDisqualifiedPlayer();
        final Player player = Bukkit.getPlayer(glassPlayer.getUniqueId());
        if (player == null) {
            return;
        }
        player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 1, 0.1, 0.1, 0.1, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
        player.setGameMode(GameMode.SPECTATOR);
        this.arena.getAnnouncer().sendGlobalMessage("&b" + glassPlayer.getName() + " &cfell off the glass bridge and died! Oh no!", false);
        this.arena.getAnnouncer().sendGlobalMessage("&7&o" + this.arena.getAlivePlayers() + " talents left...", false);
        this.arena.checkLoser();
    }
}