package com.covercorp.kaelaevent.minigame.games.platformrush.arena.listener;

import com.covercorp.kaelaevent.minigame.games.platformrush.arena.PlatformRushArena;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.event.PlatformRushDisqualificationEvent;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.event.PlatformRushTickEvent;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.state.PlatformRushMatchState;
import com.covercorp.kaelaevent.minigame.games.platformrush.inventory.PlatformRushItemCollection;
import com.covercorp.kaelaevent.minigame.games.platformrush.player.PlatformRushPlayer;

import com.covercorp.kaelaevent.util.ProgressBarUtil;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public final class PlatformRushMatchGameListener implements Listener {
    private final PlatformRushArena arena;

    public PlatformRushMatchGameListener(final PlatformRushArena arena) {
        this.arena = arena;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (arena.getState() != PlatformRushMatchState.GAME) {
            event.setCancelled(true);
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (block.getType() == Material.SNOW_BLOCK) {
            event.setDropItems(false);
            event.setCancelled(false);
            return;
        }

        player.sendMessage(arena.getPlatformRushMiniGame().getMiniMessage().deserialize(
                "<red>You can't break this block."
        ));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchTick(final PlatformRushTickEvent event) {
        if (arena.getState() != PlatformRushMatchState.GAME) return;

        // Check if any players should be dead.
        arena.getPlayerHelper().getPlayerList().stream().map(genericPlayer -> (PlatformRushPlayer) genericPlayer).forEach(gamePlayer -> {
            if (gamePlayer.isDead()) return;

            final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
            if (player == null) return;

            // Snowball action bar
            if (player.getInventory().containsAtLeast(PlatformRushItemCollection.SNOW_BALL, 16)) {
                player.sendActionBar(arena.getGameMiniMessage().deserialize(
                        "<aqua>Snowball Generator <white>" +
                                "<red>■■■■■" +
                                " <#ff6e63>[Full!]"
                ));
            } else {
                player.sendActionBar(arena.getGameMiniMessage().deserialize(
                        "<aqua>Snowball Generator <white>" +
                                ProgressBarUtil.createVanillaProgressBar(arena.getPlatformRushMatchProperties().getSnowBallCooldown(), 5, 5) +
                                " <yellow>[" + (5 - arena.getPlatformRushMatchProperties().getSnowBallCooldown()) + "s]"
                ));
            }

            final Block underneathBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

            if (underneathBlock.getType() != Material.BLACK_CONCRETE_POWDER) return;

            // The player will be disqualified.
            Bukkit.getServer().getPluginManager().callEvent(new PlatformRushDisqualificationEvent(arena, gamePlayer));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSnowball(final ProjectileHitEvent event) {
        if (arena.getState() != PlatformRushMatchState.GAME) {
            event.setCancelled(true);
            return;
        }

        if (!(event.getEntity() instanceof Snowball)) return;

        if (event.getHitEntity() instanceof final Player player) {
            final Block underneathBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (underneathBlock.getType() != Material.SNOW_BLOCK) return;

            underneathBlock.getWorld().playEffect(underneathBlock.getLocation(), Effect.STEP_SOUND, underneathBlock.getBlockData());
            underneathBlock.setType(Material.AIR);
            return;
        }

        final Block hitBlock = event.getHitBlock();
        if (hitBlock == null) return;
        if (hitBlock.getType() != Material.SNOW_BLOCK) return;

        hitBlock.getWorld().playEffect(hitBlock.getLocation(), Effect.STEP_SOUND, hitBlock.getBlockData());
        hitBlock.setType(Material.AIR);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchDisqualification(final PlatformRushDisqualificationEvent event) {
        if (arena.getState() != PlatformRushMatchState.GAME) return;

        final PlatformRushPlayer gamePlayer = event.getDisqualifiedPlayer();
        gamePlayer.setDead(true);

        final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
        if (player == null) return;

        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(arena.getArenaSpawnLocation());

        arena.getAnnouncer().sendGlobalMessage(
                "&a[Game] &b" + gamePlayer.getName() + " &chas been eliminated!",
                false
        );

        arena.checkWinner();
    }
}
