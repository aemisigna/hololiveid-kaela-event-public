package com.covercorp.kaelaevent.minigame.games.snowball.arena.listener;

import com.covercorp.kaelaevent.minigame.games.snowball.arena.SnowballArena;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.event.SnowballLightStealEvent;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.event.SnowballTickEvent;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.event.SnowballTntEvent;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.light.SnowballLightPair;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.light.light.side.LightSide;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.state.SnowballMatchState;
import com.covercorp.kaelaevent.minigame.games.snowball.inventory.SnowballItemCollection;
import com.covercorp.kaelaevent.minigame.games.snowball.player.SnowballPlayer;
import com.covercorp.kaelaevent.minigame.games.snowball.team.SnowballTeam;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import com.covercorp.kaelaevent.util.PlayerUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class SnowballMatchGameListener implements Listener {
    private final SnowballArena arena;
    private final Cache<Snowball, Boolean> snowballCache;
    private final Cache<UUID, Instant> rechargeCache;

    public SnowballMatchGameListener(final SnowballArena arena) {
        this.arena = arena;

        snowballCache = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.SECONDS).build();
        rechargeCache = CacheBuilder.newBuilder().expireAfterWrite(6L, TimeUnit.SECONDS).build();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchTick(final SnowballTickEvent event) {
        if (arena.getState() != SnowballMatchState.GAME) return;

        arena.getSnowballLightPairs().forEach((uniqueId, lightPair) -> lightPair.getLights().forEach(light -> {
            final Interaction interaction = light.getTargetHitbox();
            if (interaction == null) return;

            final BoundingBox hitbox = interaction.getBoundingBox();

            snowballCache.asMap().keySet().forEach(snowball -> {
                 if (!(snowball.getShooter() instanceof final Player player)) return;

                 final Location snowballLoc = snowball.getLocation();
                 if (!hitbox.contains(snowballLoc.toVector())) return;

                 snowballCache.invalidate(snowball);
                 snowball.remove();

                 final Optional<SnowballPlayer> snowballPlayerOptional = arena.getPlayerHelper()
                        .getPlayer(player.getUniqueId())
                        .map(generic -> (SnowballPlayer)generic);
                 if (snowballPlayerOptional.isEmpty()) return;

                 player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.9F, 0.9F);

                 final SnowballPlayer snowballPlayer = snowballPlayerOptional.get();
                 final SnowballTeam team = (SnowballTeam) snowballPlayer.getMiniGameTeam();
                 if (team == null) return;

                 final SnowballLightPair snowballLightPair = light.getPair();
                 switch (team.getIdentifier()) {
                     case "team_1" -> {
                         if (snowballLightPair.getTurnedOnLightSide() == LightSide.LEFT) return;
                         snowballLightPair.lightUpLight(LightSide.LEFT);
                     }
                     case "team_2" -> {
                         if (snowballLightPair.getTurnedOnLightSide() == LightSide.RIGHT) return;
                         snowballLightPair.lightUpLight(LightSide.RIGHT);
                     }
                }
            });
        }));

        if (arena.getExplodableTeam() != null) {
            final SnowballTeam team = arena.getExplodableTeam();
            team.getPlayers().forEach(snowballPlayer -> {
                final Player player = Bukkit.getPlayer(snowballPlayer.getUniqueId());
                if (player == null) return;

                player.getInventory().setItem(EquipmentSlot.HEAD, SnowballItemCollection.TNT);

                player.sendActionBar(
                        arena.getGameMiniMessage().deserialize("<font:minecraft:default>\uE313</font>")
                );

                player.showTitle(Title.title(
                        arena.getGameMiniMessage().deserialize(""),
                        Component.empty(),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(1000L), Duration.ofMillis(400L)))
                );
            });

            final SnowballTeam opposite = arena.getOppositeTeam(team);
            opposite.getPlayers().forEach(snowballPlayer -> {
                final Player player = Bukkit.getPlayer(snowballPlayer.getUniqueId());
                if (player == null) return;

                player.getInventory().setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR));

                player.sendActionBar(
                        arena.getGameMiniMessage().deserialize("<font:minecraft:default>\uE312</font>")
                );

                player.showTitle(Title.title(
                        arena.getGameMiniMessage().deserialize(""),
                        Component.empty(),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(1000L), Duration.ofMillis(400L)))
                );
            });
            return;
        }

        arena.getPlayerHelper().getPlayerList().forEach(snowPlayer -> {
            final Player player = Bukkit.getPlayer(snowPlayer.getUniqueId());
            if (player == null) return;

            player.sendActionBar(
                    arena.getGameMiniMessage().deserialize("<font:minecraft:default>\uE312</font>")
            );

            player.showTitle(Title.title(
                    arena.getGameMiniMessage().deserialize("\uE350"),
                    Component.empty(),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(1000L), Duration.ofMillis(400L)))
            );

            player.getEquipment().setHelmet(new ItemStack(Material.AIR));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTnt(final SnowballTntEvent event) {
        final SnowballTeam team = arena.getExplodableTeam();
        if (team == null) {
            arena.getAnnouncer().sendGlobalMessage(
                    "&aScore tied! No explosion this time! Keep going!",
                    false
            );
            arena.getAnnouncer().sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 1.2F, 1.2F);
            return;
        }

        arena.getAnnouncer().sendGlobalComponent(
                arena.getGameMiniMessage().deserialize("<#ff9c7a>Explosion emitted! Looks like </#ff9c7a><white>")
                        .append(team.getBetterPrefix())
                        .append(arena.getGameMiniMessage().deserialize("<#ff9c7a>is in trouble!"))
        );

        team.getPlayers().forEach(snowballPlayer -> {
            final Player player = Bukkit.getPlayer(snowballPlayer.getUniqueId());
            if (player == null) return;

            player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 1, 0, 0, 0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8F, 0.8F);

            if (player.getHealth() <= 4.0) {
                // ded
                player.setGameMode(GameMode.SPECTATOR);

                arena.setWinnerTeam(arena.getOppositeTeam(team));
                arena.stop();
                return;
            }

            player.setHealth(player.getHealth() - 4.0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1.2F, 1.2F);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRegeneration(final EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;

        final Optional<SnowballPlayer> gamePlayerOptional = arena.getPlayerHelper().getPlayer(player.getUniqueId()).map(mappedPlayer -> (SnowballPlayer) mappedPlayer);
        if (gamePlayerOptional.isEmpty()) return;
        
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLightSteal(final SnowballLightStealEvent event) {
        if (arena.getState() != SnowballMatchState.GAME) return;

        final LightSide lightSide = event.getTurnedOnLight();
        final int teamIndex = lightSide.getTeamIndex() + 1;

        final Optional<SnowballTeam> goodTeamOptional = arena.getTeamHelper().getTeam("team_" + teamIndex).map(g -> (SnowballTeam) g);
        if (goodTeamOptional.isEmpty()) return;

        final SnowballTeam goodTeam = goodTeamOptional.get();

        final SnowballTeam badTeam = arena.getOppositeTeam(goodTeam);
        if (badTeam == null) return;

        goodTeam.increaseScore();
        badTeam.decreaseScore();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSnowballRecharge(final PlayerInteractAtEntityEvent event) {
        if (arena.getState() != SnowballMatchState.GAME) return;

        final Player player = event.getPlayer();
        if (event.getRightClicked().getType() != EntityType.INTERACTION) return;
        if (!NBTMetadataUtil.hasEntityString(event.getRightClicked(), "ice_machine")) return;

        final Inventory inventory = player.getInventory();
        if (inventory.containsAtLeast(SnowballItemCollection.SNOWBALL, 5)) {
            player.sendMessage(arena.getGameMiniMessage().deserialize("<red>[!] You can't carry more than 5 snowballs! Go shoot!"));
            return;
        }

        inventory.addItem(new ItemBuilder(SnowballItemCollection.SNOWBALL).withAmount(1).build());
        player.playSound(player, Sound.ITEM_BUCKET_FILL_POWDER_SNOW, 0.9f, 0.9f);

        PlayerUtils.forceSwingAnimation(player);

        rechargeCache.put(player.getUniqueId(), Instant.now());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSnowball(final ProjectileLaunchEvent event) {
        if (this.arena.getState() != SnowballMatchState.GAME) {
            event.setCancelled(true);
            return;
        }

        final Projectile entity = event.getEntity();

        if (!(entity instanceof Snowball snowball)) return;
        if (!(snowball.getShooter() instanceof Player)) return;

        snowballCache.put(snowball, true);
    }
}
