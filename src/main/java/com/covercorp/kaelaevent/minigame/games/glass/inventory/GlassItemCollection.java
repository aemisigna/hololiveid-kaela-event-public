package com.covercorp.kaelaevent.minigame.games.glass.inventory;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.inventory.GlobalGlyphItemCollection;
import com.covercorp.kaelaevent.inventory.GlobalUtilityItemCollection;
import com.covercorp.kaelaevent.minigame.games.glass.player.GlassPlayer;
import com.covercorp.kaelaevent.minigame.games.glass.team.GlassTeam;
import com.covercorp.kaelaevent.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public final class GlassItemCollection {
    private static final String HOTBAR_ERROR_NULL_PLAYER = "Could not setup game hotbar for %s";

    private static final String HOTBAR_ERROR_TEAM = "<red>Can't setup your hotbar, you are not in a team!";

    public static void setupPlayerHotBar(GlassPlayer gamePlayer) {
        Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
        if (player == null) {
            KaelaEvent.getKaelaEvent().getLogger().severe(String.format(HOTBAR_ERROR_NULL_PLAYER, gamePlayer.getUniqueId()));
            return;
        }
        GlassTeam gameTeam = (GlassTeam)gamePlayer.getMiniGameTeam();
        if (gameTeam == null) {
            player.sendMessage(KaelaEvent.getKaelaEvent().getMiniGame().getMiniMessage().deserialize(HOTBAR_ERROR_TEAM));
            return;
        }
        PlayerInventory playerInventory = player.getInventory();
        player.getEquipment().setHelmet(GlobalGlyphItemCollection.BIRTHDAY_HAT_ITEM);

        if (PlayerUtils.isCool(player)) {
            playerInventory.setItem(7, GlobalUtilityItemCollection.START_GAME_ITEM);
            playerInventory.setItem(8, GlobalUtilityItemCollection.STOP_GAME_ITEM);
        }
    }

    public static void resetPlayerHotBar(GlassPlayer gamePlayer) {
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

        final AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.getModifiers().forEach(modifier -> speedAttribute.removeModifier(modifier.getKey()));
        }
        final AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null) {
            jumpAttribute.getModifiers().forEach(modifier -> jumpAttribute.removeModifier(modifier.getKey()));
        }

        final PlayerInventory playerInventory = player.getInventory();
        playerInventory.clear();
        player.getEquipment().setHelmet(GlobalGlyphItemCollection.BIRTHDAY_HAT_ITEM);

        if (PlayerUtils.isCool(player)) {
            playerInventory.setItem(7, GlobalUtilityItemCollection.START_GAME_ITEM);
            playerInventory.setItem(8, GlobalUtilityItemCollection.STOP_GAME_ITEM);
        }
    }
}