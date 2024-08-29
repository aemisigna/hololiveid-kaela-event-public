package com.covercorp.kaelaevent.minigame.games.squid.listener;

import com.covercorp.kaelaevent.inventory.GlobalGlyphItemCollection;
import com.covercorp.kaelaevent.inventory.GlobalUtilityItemCollection;
import com.covercorp.kaelaevent.minigame.games.squid.SquidMiniGame;
import com.covercorp.kaelaevent.minigame.games.squid.arena.state.SquidMatchState;
import com.covercorp.kaelaevent.minigame.games.squid.player.SquidPlayer;
import com.covercorp.kaelaevent.util.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public final class SquidAccessListener implements Listener {
    private final SquidMiniGame miniGame;

    public SquidAccessListener(final SquidMiniGame miniGame) {
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
        player.clearActivePotionEffects();
        player.setPose(Pose.STANDING, false);

        if (PlayerUtils.isCool(player)) {
            inventory.setItem(7, GlobalUtilityItemCollection.START_GAME_ITEM);
            inventory.setItem(8, GlobalUtilityItemCollection.STOP_GAME_ITEM);
        }

        player.teleport(location);

        player.sendMessage(miniGame.getMiniMessage().deserialize(
                "<newline><green>Welcome to <yellow>Red Light, Green Light<green>!"
        ));

        player.setAllowFlight(true);
        player.sendMessage(miniGame.getMiniMessage().deserialize(
                "<newline><yellow>You can fly now!"
        ));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        final Optional<SquidPlayer> gamePlayer = miniGame.getPlayerHelper().getPlayer(player.getUniqueId()).map(SquidPlayer.class::cast);

        if (gamePlayer.isPresent()) {
            miniGame.getPlayerHelper().removePlayer(player.getUniqueId());

            if (miniGame.getArena().getState() == SquidMatchState.WAITING || miniGame.getArena().getState() == SquidMatchState.ENDING) return;

            // Cancel match due to a player disconnecting
            miniGame.getAnnouncer().sendGlobalMessage("&c&l[!] The game has been cancelled due to a Talent disconnecting mid match.", true);
            miniGame.getArena().stop();
        }
    }
}
