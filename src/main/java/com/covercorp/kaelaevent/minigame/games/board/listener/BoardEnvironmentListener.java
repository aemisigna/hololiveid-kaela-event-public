package com.covercorp.kaelaevent.minigame.games.board.listener;

import com.covercorp.kaelaevent.inventory.GlobalGlyphItemCollection;
import com.covercorp.kaelaevent.inventory.GlobalUtilityItemCollection;
import com.covercorp.kaelaevent.minigame.games.board.item.BoardItemCollection;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import com.covercorp.kaelaevent.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public final class BoardEnvironmentListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinGlyph(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        for (final ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;

            if (!NBTMetadataUtil.hasString(item, "accessor") && !NBTMetadataUtil.hasString(item, "ticket_accessor")) player.getInventory().remove(item);
        }

        player.getInventory().setItem(0, BoardItemCollection.BASE_DICE_ITEM);
        player.getInventory().setItem(1, BoardItemCollection.BASE_DICE_GOLDEN_ITEM);
        player.getInventory().setItem(8, GlobalUtilityItemCollection.TELEPORTER_ITEM);

        player.getEquipment().setHelmet(GlobalGlyphItemCollection.BIRTHDAY_HAT_ITEM);

        if (!player.hasPlayedBefore()) player.teleport(new Location(player.getWorld(), 0, -59, -12.715, 0, 0));

        if (PlayerUtils.isCool(player)) {
            for (final ItemStack item : player.getInventory().getContents()) {
                if (item == null) continue;
                if (NBTMetadataUtil.hasString(item, "ticket_accessor")) player.getInventory().remove(item);
            }

            player.getInventory().setItem(7, new ItemBuilder(BoardItemCollection.SKIP_TICKET_ITEM).withAmount(64).build());
            player.setAllowFlight(true);
        }
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
    public void onPlayerDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            player.teleport(new Location(Bukkit.getWorlds().get(0), 0.5, -60, -12.5, 0, 0));
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHunger(final FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemDrop(final PlayerDropItemEvent event) {
        final ItemStack itemStack = event.getItemDrop().getItemStack();

        if (!NBTMetadataUtil.hasString(itemStack, "accessor")) return;

        event.setCancelled(true);
    }
}
