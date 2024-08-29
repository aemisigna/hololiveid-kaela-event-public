package com.covercorp.kaelaevent.minigame.games.basketball.inventory;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.inventory.GlobalGlyphItemCollection;
import com.covercorp.kaelaevent.inventory.GlobalUtilityItemCollection;
import com.covercorp.kaelaevent.minigame.games.basketball.player.BasketballPlayer;
import com.covercorp.kaelaevent.minigame.games.basketball.team.BasketballTeam;
import com.covercorp.kaelaevent.minigame.games.platformrush.player.PlatformRushPlayer;
import com.covercorp.kaelaevent.minigame.games.platformrush.team.PlatformRushTeam;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class BasketballItemCollection {
    private final static String HOTBAR_ERROR_NULL_PLAYER = "Could not setup game hotbar for %s";
    private final static String HOTBAR_ERROR_TEAM = "<red>Can't setup your hotbar, you are not in a team!";

    public final static ItemStack RAZE_SHOWSTOPPER_LOADED = new ItemBuilder(Material.DIAMOND_SHOVEL)
            .withName("&aBasketstopper &7(Right click to shoot basketballs!)")
            .withNBTTag("accessor", "raze_showstopper")
            .withCustomModel(2)
            .setUnbreakable()
            .hideStats()
            .build();
    public final static ItemStack RAZE_SHOWSTOPPER_UNLOADED = new ItemBuilder(Material.DIAMOND_SHOVEL)
            .withName("&aBasketstopper &7(Right click to shoot basketballs!)")
            .withNBTTag("accessor", "raze_showstopper")
            .withCustomModel(3)
            .setUnbreakable()
            .hideStats()
            .build();

    public final static ItemStack BASKETBALL = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withName("&aBasketball Ammo")
            .withNBTTag("accessor", "raze_ult_point")
            .withCustomModel(1101)
            .build();

    public static void setupPlayerHotBar(final BasketballPlayer gamePlayer) {
        final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());

        if (player == null) {
            KaelaEvent.getKaelaEvent().getLogger().severe(String.format(HOTBAR_ERROR_NULL_PLAYER, gamePlayer.getUniqueId()));
            return;
        }

        final BasketballTeam gameTeam = (BasketballTeam) gamePlayer.getMiniGameTeam();
        if (gameTeam == null) {
            player.sendMessage(KaelaEvent.getKaelaEvent().getMiniGame().getMiniMessage().deserialize(HOTBAR_ERROR_TEAM));
            return;
        }

        final Inventory inventory = player.getInventory();

        player.getEquipment().setHelmet(GlobalGlyphItemCollection.BIRTHDAY_HAT_ITEM);

        inventory.setItem(0, RAZE_SHOWSTOPPER_UNLOADED);

        if (PlayerUtils.isCool(player)) {
            inventory.setItem(7, GlobalUtilityItemCollection.START_GAME_ITEM);
            inventory.setItem(8, GlobalUtilityItemCollection.STOP_GAME_ITEM);
        }
    }

    public static void resetPlayerHotBar(final BasketballPlayer gamePlayer) {
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
