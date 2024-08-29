package com.covercorp.kaelaevent.minigame.games.board.inventory;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.minigame.games.board.inventory.slot.BoardSlotDisplayCollection;
import com.covercorp.kaelaevent.minigame.games.board.inventory.slot.BoardSlotItem;

import com.covercorp.kaelaevent.util.BungeeUtils;
import com.covercorp.kaelaevent.util.NegativeSpacingCollection;
import com.covercorp.kaelaevent.util.simple.Pair;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

import fr.minuskube.inv.content.SlotPos;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static java.util.Map.entry;
import java.util.Map;

public final class SlotTeleportGUI implements InventoryProvider {
    public final static SmartInventory INVENTORY = SmartInventory.builder()
            .id("KaelaTeleporter")
            .provider(new SlotTeleportGUI())
            .manager(KaelaEvent.getKaelaEvent().getInventoryManager())
            .size(6, 9)
            .title(ChatColor.WHITE + NegativeSpacingCollection.get(-24) + "\uE800")
            .build();

    private final Map<Pair<Integer, Integer>, BoardSlotItem> boardTeleportItems = Map.ofEntries(
            entry(new Pair<>(5, 1), new BoardSlotItem(BoardSlotDisplayCollection.START_SLOT, null)),
            entry(new Pair<>(5, 2), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_1, null)),
            entry(new Pair<>(5, 3), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_2, null)),
            entry(new Pair<>(5, 4), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_3, "basketball")),
            entry(new Pair<>(5, 5), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_4, null)),
            entry(new Pair<>(5, 6), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_5, "platform_rush")),
            entry(new Pair<>(4, 6), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_6, null)),
            entry(new Pair<>(4, 5), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_7, null)),
            entry(new Pair<>(4, 4), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_8, null)),
            entry(new Pair<>(4, 3), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_9, "tug")),
            entry(new Pair<>(4, 2), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_10, "color_gacha")),
            entry(new Pair<>(3, 2), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_11, null)),
            entry(new Pair<>(3, 3), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_12, "lava_roof")),
            entry(new Pair<>(3, 4), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_13, null)),
            entry(new Pair<>(3, 5), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_14, null)),
            entry(new Pair<>(3, 6), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_15, "target")),
            entry(new Pair<>(2, 6), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_16, null)),
            entry(new Pair<>(2, 5), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_17, null)),
            entry(new Pair<>(2, 4), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_18, null)),
            entry(new Pair<>(2, 3), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_19, "glass_bridge")),
            entry(new Pair<>(2, 2), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_20, null)),
            entry(new Pair<>(1, 2), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_21, null)),
            entry(new Pair<>(1, 3), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_22, "squid_game")),
            entry(new Pair<>(1, 4), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_23, "trident_race")),
            entry(new Pair<>(1, 5), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_24, null)),
            entry(new Pair<>(1, 6), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_25, "zombie")),
            entry(new Pair<>(1, 7), new BoardSlotItem(BoardSlotDisplayCollection.END_SLOT, null)),
            entry(new Pair<>(4, 8), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_SPECIAL_1, "reflex")),
            entry(new Pair<>(3, 8), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_SPECIAL_2, "snow_ball")),
            entry(new Pair<>(2, 8), new BoardSlotItem(BoardSlotDisplayCollection.SLOT_SPECIAL_3, "tower"))
    );

    @Override
    public void init(final Player player, final InventoryContents contents) {
        boardTeleportItems.forEach((k, v) -> {
            contents.set(SlotPos.of(k.key(), k.value() ), ClickableItem.of(v.getItemStack(), e -> {
                player.closeInventory();
                final String server = v.getServer();
                if (server != null) {
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> BungeeUtils.sendPlayerToServer(onlinePlayer, server));
                }
            }));
        });
    }

    @Override
    public void update(final Player player, final InventoryContents contents) {}
}
