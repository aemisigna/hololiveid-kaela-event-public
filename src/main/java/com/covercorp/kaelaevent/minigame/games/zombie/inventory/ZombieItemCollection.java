package com.covercorp.kaelaevent.minigame.games.zombie.inventory;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.inventory.GlobalGlyphItemCollection;
import com.covercorp.kaelaevent.inventory.GlobalUtilityItemCollection;
import com.covercorp.kaelaevent.minigame.games.zombie.player.ZombiePlayer;
import com.covercorp.kaelaevent.minigame.games.zombie.team.ZombieTeam;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class ZombieItemCollection {
    private final static String HOTBAR_ERROR_NULL_PLAYER = "Could not setup game hotbar for %s";
    private final static String HOTBAR_ERROR_TEAM = "<red>Can't setup your hotbar, you are not in a team!";

    public static final ItemStack BOW_ITEM = new ItemBuilder(Material.BOW)
            .withName("&aBow")
            .withNBTTag("accessor", "zombie_bow")
            .withEnchantment(Enchantment.INFINITY)
            .setUnbreakable()
            .hideEnchantments()
            .hideStats()
            .build();
    public static final ItemStack ARROW = new ItemBuilder(Material.ARROW)
            .withNBTTag("accessor", "zombie_arrow")
            .withName("&aArrow")
            .build();

    public static final ItemStack GOLD_HELMET = new ItemBuilder(Material.GOLDEN_HELMET)
            .withNBTTag("accessor", "zombie_gold_helmet")
            .withName("&aGold Armor")
            .build();
    public static final ItemStack GOLD_CHESTPLATE = new ItemBuilder(Material.GOLDEN_HELMET)
            .withNBTTag("accessor", "zombie_gold_chestplate")
            .withName("&aGold Armor")
            .build();
    public static final ItemStack GOLD_LEGGINGS = new ItemBuilder(Material.GOLDEN_HELMET)
            .withNBTTag("accessor", "zombie_gold_leggings")
            .withName("&aGold Armor")
            .build();
    public static final ItemStack GOLD_BOOTS = new ItemBuilder(Material.GOLDEN_HELMET)
            .withNBTTag("accessor", "zombie_gold_boots")
            .withName("&aGold Armor")
            .build();
    public static final ItemStack TNT_HELMET = new ItemBuilder(Material.TNT)
            .withNBTTag("accessor", "zombie_tnt_helmet")
            .withName("&aTNT")
            .build();


    public static void setupPlayerHotBar(final ZombiePlayer gamePlayer) {
        final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());

        if (player == null) {
            KaelaEvent.getKaelaEvent().getLogger().severe(String.format(HOTBAR_ERROR_NULL_PLAYER, gamePlayer.getUniqueId()));
            return;
        }

        final ZombieTeam gameTeam = (ZombieTeam) gamePlayer.getMiniGameTeam();
        if (gameTeam == null) {
            player.sendMessage(KaelaEvent.getKaelaEvent().getMiniGame().getMiniMessage().deserialize(HOTBAR_ERROR_TEAM));
            return;
        }

        final Inventory inventory = player.getInventory();

        player.getEquipment().setHelmet(GlobalGlyphItemCollection.BIRTHDAY_HAT_ITEM);

        inventory.setItem(0, BOW_ITEM);
        inventory.setItem(1, ARROW);

        if (PlayerUtils.isCool(player)) {
            inventory.setItem(7, GlobalUtilityItemCollection.START_GAME_ITEM);
            inventory.setItem(8, GlobalUtilityItemCollection.STOP_GAME_ITEM);
        }
    }

    public static void resetPlayerHotBar(final ZombiePlayer gamePlayer) {
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
