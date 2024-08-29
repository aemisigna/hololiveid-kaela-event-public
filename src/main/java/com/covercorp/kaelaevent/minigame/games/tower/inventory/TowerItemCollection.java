package com.covercorp.kaelaevent.minigame.games.tower.inventory;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.inventory.GlobalGlyphItemCollection;
import com.covercorp.kaelaevent.inventory.GlobalUtilityItemCollection;
import com.covercorp.kaelaevent.minigame.games.tower.player.TowerPlayer;
import com.covercorp.kaelaevent.minigame.games.tower.team.TowerTeam;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class TowerItemCollection {
    private static final String HOTBAR_ERROR_NULL_PLAYER = "Could not setup game hotbar for %s";

    private static final String HOTBAR_ERROR_TEAM = "<red>Can't setup your hotbar, you are not in a team!";

    public static final ItemStack GAMBLING_MACHINE_DEFAULT_LEVER_UP = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(59)
            .withNBTTag("accessor", "gambling_default_lever_up")
            .build();
    public static final ItemStack GAMBLING_MACHINE_DEFAULT_LEVER_DOWN = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(60)
            .withNBTTag("accessor", "gambling_default_lever_down")
            .build();
    public static final ItemStack GAMBLING_MACHINE_ROLLING = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(61)
            .withNBTTag("accessor", "gambling_default_lever_rolling")
            .build();
    public static final ItemStack GAMBLING_MACHINE_REDSTONE = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(62)
            .withNBTTag("accessor", "gambling_default_lever_redstone")
            .build();
    public static final ItemStack GAMBLING_MACHINE_GOLD = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(63)
            .withNBTTag("accessor", "gambling_default_lever_gold")
            .build();
    public static final ItemStack GAMBLING_MACHINE_EMERALD = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(64)
            .withNBTTag("accessor", "gambling_default_lever_emerald")
            .build();
    public static final ItemStack GAMBLING_MACHINE_DIAMOND = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(65)
            .withNBTTag("accessor", "gambling_default_lever_diamond")
            .build();
    public static final ItemStack GAMBLING_MACHINE_NETHERITE = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(66)
            .withNBTTag("accessor", "gambling_default_lever_netherite")
            .build();

    public static void setupPlayerHotBar(TowerPlayer gamePlayer) {
        final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
        if (player == null) {
            KaelaEvent.getKaelaEvent().getLogger().severe(String.format(HOTBAR_ERROR_NULL_PLAYER, gamePlayer.getUniqueId()));
            return;
        }

        final TowerTeam gameTeam = (TowerTeam) gamePlayer.getMiniGameTeam();
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

    public static void resetPlayerHotBar(TowerPlayer gamePlayer) {
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