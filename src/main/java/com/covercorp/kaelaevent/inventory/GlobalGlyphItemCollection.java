package com.covercorp.kaelaevent.inventory;

import com.covercorp.kaelaevent.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class GlobalGlyphItemCollection {
    public static final ItemStack BIRTHDAY_HAT_ITEM = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&7Dice Event Birthday Hat")
            .withNBTTag("accessor", "bday_hat")
            .withCustomModel(6)
            .build();
}
