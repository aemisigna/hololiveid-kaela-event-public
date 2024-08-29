package com.covercorp.kaelaevent.minigame.games.board.dice.listener;

import com.covercorp.kaelaevent.minigame.games.board.dice.DiceRollerHelper;
import com.covercorp.kaelaevent.minigame.games.board.dice.type.DiceRollType;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public final class BoardDiceListener implements Listener {
    private final DiceRollerHelper diceRollerHelper;

    public BoardDiceListener(final DiceRollerHelper diceRollerHelper) {
        this.diceRollerHelper = diceRollerHelper;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemDrop(final PlayerDropItemEvent event) {
        final ItemStack itemStack = event.getItemDrop().getItemStack();

        if (!NBTMetadataUtil.hasString(itemStack, "accessor")) return;

        final String accessor = NBTMetadataUtil.getString(itemStack, "accessor");

        if (accessor != null && accessor.equalsIgnoreCase("dice")) {
            diceRollerHelper.roll(event.getPlayer(), DiceRollType.BOARD);
            event.setCancelled(true);
        } else if (accessor != null && accessor.equalsIgnoreCase("event")) {
            diceRollerHelper.roll(event.getPlayer(), DiceRollType.EVENT);
            event.setCancelled(true);
        }
    }
}
