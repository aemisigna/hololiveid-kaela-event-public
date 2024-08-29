package com.covercorp.kaelaevent.minigame.games.colorgacha.inventory;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.inventory.GlobalGlyphItemCollection;
import com.covercorp.kaelaevent.inventory.GlobalUtilityItemCollection;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.state.ButtonColor;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.state.ButtonStatus;
import com.covercorp.kaelaevent.minigame.games.colorgacha.player.ColorGachaPlayer;
import com.covercorp.kaelaevent.minigame.games.colorgacha.team.ColorGachaTeam;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class ColorGachaItemCollection {
    private final static String HOTBAR_ERROR_NULL_PLAYER = "Could not setup game hotbar for %s";
    private final static String HOTBAR_ERROR_TEAM = "<red>Can't setup your hotbar, you are not in a team!";

    private final static ItemStack STAND_RED = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("stand_accessor", "stand_red")
            .withCustomModel(28)
            .build();
    private final static ItemStack STAND_PURPLE = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("stand_accessor", "stand_purple")
            .withCustomModel(29)
            .build();
    private final static ItemStack STAND_YELLOW = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("stand_accessor", "stand_yellow")
            .withCustomModel(30)
            .build();
    private final static ItemStack STAND_GREEN = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("stand_accessor", "stand_green")
            .withCustomModel(31)
            .build();
    private final static ItemStack STAND_WHITE = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("stand_accessor", "stand_white")
            .withCustomModel(32)
            .build();

    private final static ItemStack STAND_RED_PRESSED = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("stand_accessor", "stand_red_pressed")
            .withCustomModel(33)
            .build();
    private final static ItemStack STAND_PURPLE_PRESSED = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("stand_accessor", "stand_purple_pressed")
            .withCustomModel(34)
            .build();
    private final static ItemStack STAND_YELLOW_PRESSED = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("stand_accessor", "stand_yellow")
            .withCustomModel(35)
            .build();
    private final static ItemStack STAND_GREEN_PRESSED = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("stand_accessor", "stand_green_pressed")
            .withCustomModel(36)
            .build();
    private final static ItemStack STAND_WHITE_PRESSED = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("stand_accessor", "stand_white_pressed")
            .withCustomModel(37)
            .build();

    public static ItemStack getStand(final ButtonColor color, final ButtonStatus status) {
        if (status == ButtonStatus.UNPRESSED) {
            switch (color) {
                case RED -> {
                    return STAND_RED;
                }
                case PURPLE -> {
                    return STAND_PURPLE;
                }
                case YELLOW-> {
                    return STAND_YELLOW;
                }
                case GREEN -> {
                    return STAND_GREEN;
                }
                case WHITE -> {
                    return STAND_WHITE;
                }
            }
        }

        if (status == ButtonStatus.PRESSED) {
            switch (color) {
                case RED -> {
                    return STAND_RED_PRESSED;
                }
                case PURPLE -> {
                    return STAND_PURPLE_PRESSED;
                }
                case YELLOW-> {
                    return STAND_YELLOW_PRESSED;
                }
                case GREEN -> {
                    return STAND_GREEN_PRESSED;
                }
                case WHITE -> {
                    return STAND_WHITE_PRESSED;
                }
            }
        }

        return new ItemStack(Material.DIAMOND_BLOCK);
    }

    public static void setupPlayerHotBar(final ColorGachaPlayer gamePlayer) {
        final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());

        if (player == null) {
            KaelaEvent.getKaelaEvent().getLogger().severe(String.format(HOTBAR_ERROR_NULL_PLAYER, gamePlayer.getUniqueId()));
            return;
        }

        final ColorGachaTeam gameTeam = (ColorGachaTeam) gamePlayer.getMiniGameTeam();
        if (gameTeam == null) {
            player.sendMessage(KaelaEvent.getKaelaEvent().getMiniGame().getMiniMessage().deserialize(HOTBAR_ERROR_TEAM));
            return;
        }

        final Inventory inventory = player.getInventory();

        player.getEquipment().setHelmet(GlobalGlyphItemCollection.BIRTHDAY_HAT_ITEM);

        if (PlayerUtils.isCool(player)) {
            inventory.setItem(7, GlobalUtilityItemCollection.START_GAME_ITEM);
            inventory.setItem(8, GlobalUtilityItemCollection.STOP_GAME_ITEM);
        }
    }

    public static void resetPlayerHotBar(final ColorGachaPlayer gamePlayer) {
        final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());

        if (player == null) {
            KaelaEvent.getKaelaEvent().getLogger().severe(String.format(HOTBAR_ERROR_NULL_PLAYER, gamePlayer.getUniqueId()));
            return;
        }

        player.setExp(0);
        player.setLevel(0);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        final Inventory inventory = player.getInventory();

        inventory.clear();

        player.getEquipment().setHelmet(GlobalGlyphItemCollection.BIRTHDAY_HAT_ITEM);

        if (PlayerUtils.isCool(player)) {
            inventory.setItem(7, GlobalUtilityItemCollection.START_GAME_ITEM);
            inventory.setItem(8, GlobalUtilityItemCollection.STOP_GAME_ITEM);
        }
    }
}
