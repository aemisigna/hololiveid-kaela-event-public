package com.covercorp.kaelaevent.minigame.games.board.item;

import com.covercorp.kaelaevent.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class BoardItemCollection {
    public static final ItemStack BASE_DICE_ITEM = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&aBoard Dice &7(Press Q to roll the dice!)")
            .withNBTTag("accessor", "dice")
            .withCustomModel(500)
            .build();
    public static final ItemStack BASE_DICE = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&7Rolling Dice...")
            .withCustomModel(500)
            .build();
    public static final ItemStack ONE_DICE = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&7Number rolled: 1")
            .withCustomModel(501)
            .build();
    public static final ItemStack TWO_DICE = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&7Number rolled: 2")
            .withCustomModel(502)
            .build();
    public static final ItemStack THREE_DICE = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&7Number rolled: 3")
            .withCustomModel(503)
            .build();
    public static final ItemStack FOUR_DICE = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&7Number rolled: 4")
            .withCustomModel(504)
            .build();
    public static final ItemStack REROLL_DICE = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&7Reroll!")
            .withCustomModel(505)
            .build();

    public static final ItemStack BASE_DICE_GOLDEN_ITEM = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&6Event Dice &7(Press Q to roll the dice!)")
            .withNBTTag("accessor", "event")
            .withCustomModel(506)
            .build();
    public static final ItemStack BASE_DICE_GOLDEN = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&7Rolling Event...")
            .withCustomModel(506)
            .build();
    public static final ItemStack ROLLED_DICE_GOLDEN_ITEM = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&7Event!")
            .withCustomModel(507)
            .build();

    public static final ItemStack SKIP_TICKET_ITEM = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&dSkip Challenge Item")
            .withNBTTag("ticket_accessor", "skip_ticket")
            .withCustomModel(45)
            .build();
}