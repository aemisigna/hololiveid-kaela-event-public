package com.covercorp.kaelaevent.minigame.games.snowball.inventory;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.inventory.GlobalGlyphItemCollection;
import com.covercorp.kaelaevent.inventory.GlobalUtilityItemCollection;
import com.covercorp.kaelaevent.minigame.games.snowball.player.SnowballPlayer;
import com.covercorp.kaelaevent.minigame.games.snowball.team.SnowballTeam;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class SnowballItemCollection {
    private static final String HOTBAR_ERROR_NULL_PLAYER = "Could not setup game hotbar for %s";

    private static final String HOTBAR_ERROR_TEAM = "<red>Can't setup your hotbar, you are not in a team!";

    public static final ItemStack SNOWBALL = new ItemBuilder(Material.SNOWBALL)
            .withNBTTag("accessor", "snow_ball")
            .build();
    public static final ItemStack TNT = new ItemBuilder(Material.TNT)
            .withNBTTag("accessor", "tnt")
            .build();
    public static final ItemStack SCORE_MACHINE_0 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("accessor", "score_machine")
            .withCustomModel(51)
            .build();
    public static final ItemStack SCORE_MACHINE_1 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("accessor", "score_machine")
            .withCustomModel(52)
            .build();
    public static final ItemStack SCORE_MACHINE_2 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("accessor", "score_machine")
            .withCustomModel(53)
            .build();
    public static final ItemStack SCORE_MACHINE_3 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("accessor", "score_machine")
            .withCustomModel(54)
            .build();
    public static final ItemStack SCORE_MACHINE_4 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("accessor", "score_machine")
            .withCustomModel(55)
            .build();
    public static final ItemStack SCORE_MACHINE_5 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("accessor", "score_machine")
            .withCustomModel(56)
            .build();
    public static final ItemStack SCORE_MACHINE_6 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withNBTTag("accessor", "score_machine")
            .withCustomModel(57)
            .build();

    public static void setupPlayerHotBar(SnowballPlayer gamePlayer) {
        final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
        if (player == null) {
            KaelaEvent.getKaelaEvent().getLogger().severe(String.format(HOTBAR_ERROR_NULL_PLAYER, gamePlayer.getUniqueId()));
            return;
        }

        final SnowballTeam gameTeam = (SnowballTeam)gamePlayer.getMiniGameTeam();
        if (gameTeam == null) {
            player.sendMessage(KaelaEvent.getKaelaEvent().getMiniGame().getMiniMessage().deserialize(HOTBAR_ERROR_TEAM));
            return;
        }

        final PlayerInventory playerInventory = player.getInventory();

        player.getEquipment().setHelmet(GlobalGlyphItemCollection.BIRTHDAY_HAT_ITEM);

        playerInventory.setItem(0, new ItemBuilder(SNOWBALL).withAmount(5).build());

        if (PlayerUtils.isCool(player)) {
            playerInventory.setItem(7, GlobalUtilityItemCollection.START_GAME_ITEM);
            playerInventory.setItem(8, GlobalUtilityItemCollection.STOP_GAME_ITEM);
        }
    }

    public static void resetPlayerHotBar(SnowballPlayer gamePlayer) {
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