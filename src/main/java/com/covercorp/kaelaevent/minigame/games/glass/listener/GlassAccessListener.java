package com.covercorp.kaelaevent.minigame.games.glass.listener;

import com.covercorp.kaelaevent.inventory.GlobalGlyphItemCollection;
import com.covercorp.kaelaevent.inventory.GlobalUtilityItemCollection;
import com.covercorp.kaelaevent.minigame.games.glass.GlassMiniGame;
import com.covercorp.kaelaevent.minigame.games.glass.arena.state.GlassMatchState;
import com.covercorp.kaelaevent.minigame.games.glass.player.GlassPlayer;
import com.covercorp.kaelaevent.util.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public final class GlassAccessListener implements Listener {
    private final GlassMiniGame miniGame;

    public GlassAccessListener(final GlassMiniGame miniGame) {
        this.miniGame = miniGame;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final Location location = miniGame.getConfigHelper().getLobbySpawn();
        final Inventory inventory = player.getInventory();

        player.getEquipment().clear();
        inventory.clear();

        player.getEquipment().setHelmet(GlobalGlyphItemCollection.BIRTHDAY_HAT_ITEM);
        if (PlayerUtils.isCool(player)) {
            inventory.setItem(7, GlobalUtilityItemCollection.START_GAME_ITEM);
            inventory.setItem(8, GlobalUtilityItemCollection.STOP_GAME_ITEM);
        }

        player.teleport(location);

        player.sendMessage(miniGame.getMiniMessage().deserialize(
                "<newline><green>Welcome to <yellow>Glass Bridge Crossing<green>!"
        ));

        player.setAllowFlight(true);
        player.sendMessage(miniGame.getMiniMessage().deserialize(
                "<newline><yellow>You can fly now!"
        ));

        final AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.getModifiers().forEach(modifier -> speedAttribute.removeModifier(modifier.getKey()));
        }
        final AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null) {
            jumpAttribute.getModifiers().forEach(modifier -> jumpAttribute.removeModifier(modifier.getKey()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        final Optional<GlassPlayer> gamePlayer = miniGame.getPlayerHelper().getPlayer(player.getUniqueId()).map(GlassPlayer.class::cast);

        if (gamePlayer.isPresent()) {
            miniGame.getPlayerHelper().removePlayer(player.getUniqueId());

            if (miniGame.getArena().getState() == GlassMatchState.WAITING || miniGame.getArena().getState() == GlassMatchState.ENDING) return;

            // Cancel match due to a player disconnecting
            miniGame.getAnnouncer().sendGlobalMessage("&c&l[!] The game has been cancelled due to a Talent disconnecting mid match.", true);
            miniGame.getArena().stop();
        }
    }
}
