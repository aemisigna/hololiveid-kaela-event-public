package com.covercorp.kaelaevent.inventory;

import com.covercorp.kaelaevent.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class GlobalUtilityItemCollection {
    public static final ItemStack TELEPORTER_ITEM = new ItemBuilder(Material.RECOVERY_COMPASS)
            .withName("&eGame Teleporter &7(Click to open the menu)")
            .withNBTTag("accessor", "teleporter")
            .build();
    public static final ItemStack START_GAME_ITEM = new ItemBuilder(Material.DIAMOND)
            .withName("&aStart game &7(Click to use)")
            .withCustomModel(1)
            .withNBTTag("accessor", "start_game")
            .build();
    public static final ItemStack STOP_GAME_ITEM = new ItemBuilder(Material.DIAMOND)
            .withName("&cStop game &7(Click to use)")
            .withCustomModel(2)
            .withNBTTag("accessor", "stop_game")
            .build();
}
