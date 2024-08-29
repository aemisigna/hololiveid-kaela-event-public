package com.covercorp.kaelaevent.minigame.games.board.listener;

import com.covercorp.kaelaevent.minigame.games.board.inventory.SlotTeleportGUI;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class BoardItemListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemClick(final PlayerInteractEvent event) {
        final Player sender = event.getPlayer();
        final ItemStack itemStack = sender.getInventory().getItemInMainHand();

        if (itemStack.getType() == Material.AIR) return;
        if (!NBTMetadataUtil.hasString(itemStack, "accessor")) return;

        event.setCancelled(true);

        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

        final String accessor = NBTMetadataUtil.getString(itemStack, "accessor");

        if (accessor != null) {
            if (accessor.equals("teleporter")) {
                SlotTeleportGUI.INVENTORY.open(sender);
            }
        }
    }
}
