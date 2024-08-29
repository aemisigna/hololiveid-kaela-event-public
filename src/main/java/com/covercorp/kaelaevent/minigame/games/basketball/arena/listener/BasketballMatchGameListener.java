package com.covercorp.kaelaevent.minigame.games.basketball.arena.listener;

import com.covercorp.kaelaevent.minigame.games.basketball.arena.BasketballArena;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.ball.ShootedBasketball;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.event.BasketballTickEvent;
import com.covercorp.kaelaevent.minigame.games.basketball.arena.state.BasketballMatchState;
import com.covercorp.kaelaevent.minigame.games.basketball.inventory.BasketballItemCollection;
import com.covercorp.kaelaevent.minigame.games.basketball.player.BasketballPlayer;

import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class BasketballMatchGameListener implements Listener {
    private final BasketballArena arena;

    public BasketballMatchGameListener(final BasketballArena arena) {
        this.arena = arena;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchTick(final BasketballTickEvent event) {
        if (arena.getState() != BasketballMatchState.GAME) return;

        arena.getPlayerHelper().getPlayerList().stream().map(genericPlayer -> (BasketballPlayer) genericPlayer).forEach(gamePlayer -> {
            final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
            if (player == null) return;

            // Check if the player has any ammo, so I change the basketstopper texture
            final Inventory inventory = player.getInventory();
            final ItemStack firstItem = inventory.getItem(0);

            if (inventory.containsAtLeast(BasketballItemCollection.BASKETBALL, 1)) {
                if (firstItem == null) {
                    inventory.setItem(0, BasketballItemCollection.RAZE_SHOWSTOPPER_LOADED);
                    return;
                }
                // The player has enough ammo.
                if (firstItem.equals(BasketballItemCollection.RAZE_SHOWSTOPPER_LOADED)) return;

                inventory.setItem(0, BasketballItemCollection.RAZE_SHOWSTOPPER_LOADED);
            } else {
                if (firstItem == null) {
                    inventory.setItem(0, BasketballItemCollection.RAZE_SHOWSTOPPER_UNLOADED);
                    return;
                }

                if (firstItem.equals(BasketballItemCollection.RAZE_SHOWSTOPPER_UNLOADED)) return;

                inventory.setItem(0, BasketballItemCollection.RAZE_SHOWSTOPPER_UNLOADED);
            }
        });

        arena.getShootedBasketballs().forEach((uuid, shootedBasketball) -> shootedBasketball.getEntity().getLocation().getWorld().spawnParticle(Particle.FIREWORK, shootedBasketball.getEntity().getLocation(), 1, 0, 0, 0, 0));

        final Location basketHitBox = arena.getHitBoxLocation();
        basketHitBox.getNearbyEntitiesByType(ArmorStand.class, 0.8).forEach(detectedArmorStand -> {
            final UUID armorStandUniqueId = detectedArmorStand.getUniqueId();
            final ShootedBasketball possibleShootedBasketball = arena.getShootedBasketballs().get(armorStandUniqueId);
            if (possibleShootedBasketball == null) return;

            // The shooter scored!
            final BasketballPlayer shooter = possibleShootedBasketball.getShooter();
            shooter.setScore(shooter.getScore() + 1);

            final Firework firework = basketHitBox.getWorld().spawn(basketHitBox, Firework.class);
            final FireworkMeta fireworkMeta = firework.getFireworkMeta();

            fireworkMeta.setPower(2);
            fireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.GREEN).withFade(Color.YELLOW).build());

            firework.setFireworkMeta(fireworkMeta);
            firework.detonate();

            basketHitBox.getWorld().playSound(basketHitBox, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8F, 0.8F);
            basketHitBox.getWorld().spawnParticle(Particle.FIREWORK, basketHitBox, 50, 0.5, 0.5, 0.5, 0.1);
            arena.getAnnouncer().sendGlobalMessage("&a[Game] &b" + shooter.getName() + " &escored a point for their team!", false);

            arena.getShootedBasketballs().remove(possibleShootedBasketball.getEntity().getUniqueId());
            possibleShootedBasketball.deSpawn();
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemPickup(final PlayerAttemptPickupItemEvent event) {
        if (arena.getState() != BasketballMatchState.GAME) return;

        final Player player = event.getPlayer();
        final Inventory inventory = player.getInventory();

        final Optional<BasketballPlayer> gamePlayerOptional = arena.getPlayerHelper().getPlayer(player.getUniqueId()).map(genericPlayer -> (BasketballPlayer) genericPlayer);
        if (gamePlayerOptional.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        if (inventory.containsAtLeast(BasketballItemCollection.BASKETBALL, 1)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCannonClick(final PlayerInteractEvent event) {
        if (arena.getState() != BasketballMatchState.GAME) return;

        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();
        if (item == null) return;

        if (!NBTMetadataUtil.hasString(item, "accessor")) return;
        if (!NBTMetadataUtil.getString(item, "accessor").equals("raze_showstopper")) return;

        if (arena.getHitBoxLocation().getNearbyPlayers(2, 8, 2).contains(player)) {
            player.playSound(player, Sound.BLOCK_ANVIL_HIT, 2.0F, 0.5F);
            player.showTitle(Title.title(Component.empty(), arena.getGameMiniMessage().deserialize("<red>You can't shoot this close to the basket!"), Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)));
            player.sendMessage(arena.getGameMiniMessage().deserialize(
                    "<red>[!!] You can't shoot this close to the basket! Get away!"
            ));
            return;
        }
        // The player is trying to shoot the showstopper
        final Optional<BasketballPlayer> gamePlayerOptional = arena.getPlayerHelper().getPlayer(player.getUniqueId()).map(genericPlayer -> (BasketballPlayer) genericPlayer);
        if (gamePlayerOptional.isEmpty()) return;

        // The player indeed is a talent participating
        final Inventory inventory = player.getInventory();
        if (!inventory.containsAtLeast(BasketballItemCollection.BASKETBALL, 1)) {
            player.playSound(player, Sound.BLOCK_ANVIL_HIT, 2.0F, 0.5F);
            player.showTitle(Title.title(Component.empty(), arena.getGameMiniMessage().deserialize("<red>Not enough basketballs to shoot!"), Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)));
            player.sendMessage(arena.getGameMiniMessage().deserialize(
                    "<red>[!!] You don't have any basketballs to shoot the Basketstopper!"
            ));
            return;
        }

        // Remove one ball
        inventory.removeItem(BasketballItemCollection.BASKETBALL);

        final BasketballPlayer gamePlayer = gamePlayerOptional.get();

        // Shoot
        final ShootedBasketball shootedBasketball = new ShootedBasketball(gamePlayer, player.getEyeLocation());
        arena.getBasketballMiniGame().getTimedEntityHelper().makeEntity(shootedBasketball, true);

        arena.getShootedBasketballs().put(shootedBasketball.getEntity().getUniqueId(), shootedBasketball);
        // Shooted basketball extends ArmorStand, we must shoot it using an angle and vectors, just like the dice, but more powerful

        final Vector directionVector = player.getLocation().getDirection();
        final Vector launchVector = directionVector.clone().multiply(0.8).add(new Vector(0, 0.6, 0));

        // Shoot the ball
        shootedBasketball.getEntity().setVelocity(launchVector);

        // Apply recoil effect to the player
        final Vector recoilVector = directionVector.clone().multiply(-0.8).add(new Vector(0, 0.4, 0));
        player.setVelocity(player.getVelocity().add(recoilVector));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.5F, 0.5F);
    }
}
