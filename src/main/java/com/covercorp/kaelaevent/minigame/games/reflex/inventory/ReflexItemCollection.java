package com.covercorp.kaelaevent.minigame.games.reflex.inventory;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.inventory.GlobalGlyphItemCollection;
import com.covercorp.kaelaevent.inventory.GlobalUtilityItemCollection;
import com.covercorp.kaelaevent.minigame.games.reflex.player.ReflexPlayer;
import com.covercorp.kaelaevent.minigame.games.reflex.team.ReflexTeam;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class ReflexItemCollection {
    private static final String HOTBAR_ERROR_NULL_PLAYER = "Could not setup game hotbar for %s";

    private static final String HOTBAR_ERROR_TEAM = "<red>Can't setup your hotbar, you are not in a team!";

    public static final ItemStack REFLEX_SCREEN_WAIT = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(70)
            .withNBTTag("accessor", "screen_wait")
            .build();
    public static final ItemStack REFLEX_SCREEN_PRESS = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(71)
            .withNBTTag("accessor", "screen_press")
            .build();
    public static final ItemStack REFLEX_SCREEN_NICE = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(72)
            .withNBTTag("accessor", "screen_nice")
            .build();
    public static final ItemStack REFLEX_SCREEN_BAD = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(73)
            .withNBTTag("accessor", "screen_bad")
            .build();
    public static final ItemStack REFLEX_BUTTON_UNPRESSED = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(69)
            .withNBTTag("accessor", "button_unpressed")
            .build();
    public static final ItemStack REFLEX_BUTTON_PRESSED = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(68)
            .withNBTTag("accessor", "button_pressed")
            .build();
    public static final ItemStack REFLEX_CHAIR = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(67)
            .withNBTTag("accessor", "chair")
            .build();

    public static void setupPlayerHotBar(ReflexPlayer gamePlayer) {
        final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
        if (player == null) {
            KaelaEvent.getKaelaEvent().getLogger().severe(String.format(HOTBAR_ERROR_NULL_PLAYER, gamePlayer.getUniqueId()));
            return;
        }

        final ReflexTeam gameTeam = (ReflexTeam) gamePlayer.getMiniGameTeam();
        if (gameTeam == null) {
            player.sendMessage(KaelaEvent.getKaelaEvent().getMiniGame().getMiniMessage().deserialize(HOTBAR_ERROR_TEAM));
            return;
        }

        final PlayerInventory playerInventory = player.getInventory();

        player.getEquipment().setHelmet(GlobalGlyphItemCollection.BIRTHDAY_HAT_ITEM);

        if (PlayerUtils.isCool(player)) {
            playerInventory.setItem(7, GlobalUtilityItemCollection.START_GAME_ITEM);
            playerInventory.setItem(8, GlobalUtilityItemCollection.STOP_GAME_ITEM);
        }
    }

    public static void resetPlayerHotBar(ReflexPlayer gamePlayer) {
        Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
        if (player == null) {
            KaelaEvent.getKaelaEvent().getLogger().severe(String.format("Could not setup game hotbar for %s", gamePlayer.getUniqueId()));
            return;
        }

        player.setExp(0.0F);
        player.setLevel(0);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        final PlayerInventory playerInventory = player.getInventory();
        playerInventory.clear();

        player.getEquipment().setHelmet(GlobalGlyphItemCollection.BIRTHDAY_HAT_ITEM);

        if (PlayerUtils.isCool(player)) {
            playerInventory.setItem(7, GlobalUtilityItemCollection.START_GAME_ITEM);
            playerInventory.setItem(8, GlobalUtilityItemCollection.STOP_GAME_ITEM);
        }
    }
}