package com.covercorp.kaelaevent.minigame.games.tower.listener;

import com.covercorp.kaelaevent.minigame.games.tower.TowerMiniGame;
import com.covercorp.kaelaevent.minigame.games.tower.arena.TowerArena;
import com.covercorp.kaelaevent.minigame.games.tower.arena.state.TowerMatchState;
import com.covercorp.kaelaevent.minigame.games.tower.player.TowerPlayer;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public final class TowerStatsListener implements Listener {
    private final TowerMiniGame miniGame;

    private final TowerArena arena;

    public TowerStatsListener(final TowerMiniGame miniGame) {
        this.miniGame = miniGame;

        arena = miniGame.getArena();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHungerChange(final FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        player.setFoodLevel(20);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemClick(final InventoryClickEvent event) {
        if (event.getHotbarButton() >= 0 && event.getHotbarButton() < 9) {
            event.setCancelled(true);
            return;
        }

        final ItemStack item = event.getCurrentItem();
        final ItemStack cursorItem = event.getCursor();

        if (item != null) {
            if (NBTMetadataUtil.hasString(item, "accessor")) event.setCancelled(true);
            return;
        }

        if (NBTMetadataUtil.hasString(cursorItem, "accessor")) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemDrop(final PlayerDropItemEvent event) {
        final ItemStack itemStack = event.getItemDrop().getItemStack();

        if (!NBTMetadataUtil.hasString(itemStack, "accessor")) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            event.setCancelled(true);
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID  && player.getLocation().getY() < 0) {
            new BukkitRunnable() {
                public void run() {
                    final Location lobby = miniGame.getArena().getLobbyLocation().clone();
                    lobby.setY(lobby.getY() + 1);

                    player.teleport(lobby);
                    player.setFallDistance(0F);

                    cancel();
                }
            }.runTaskLater(miniGame.getKaelaEvent(), 1L);
        }

        if (arena.getState() == TowerMatchState.WAITING) {
            event.setCancelled(true);
            return;
        }

        final Optional<TowerPlayer> gamePlayerOptional = arena.getPlayerHelper().getPlayer(player.getUniqueId())
                .map(mappedPlayer -> (TowerPlayer) mappedPlayer);
        if (gamePlayerOptional.isEmpty()) {
            event.setCancelled(true);
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) event.setCancelled(true);
    }
}
