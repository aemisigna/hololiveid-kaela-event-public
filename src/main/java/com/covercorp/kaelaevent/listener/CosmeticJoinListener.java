package com.covercorp.kaelaevent.listener;

import com.covercorp.kaelaevent.KaelaEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;

public final class CosmeticJoinListener implements Listener {
    private final KaelaEvent kaelaEvent;

    public CosmeticJoinListener(final KaelaEvent kaelaEvent) {
        this.kaelaEvent = kaelaEvent;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        player.showTitle(Title.title(
                Component.text("\uE299\uE299\uE299\uE299\uE299\uE299\uE299"),
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(1500),
                        Duration.ofMillis(500)
                )
        ));

        event.joinMessage(kaelaEvent.getMiniGame().getMiniMessage().deserialize(
                "<gray><italic>" + player.getName() + " moved into the game arena."
        ));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        event.quitMessage(kaelaEvent.getMiniGame().getMiniMessage().deserialize(
                "<gray><italic>" + player.getName() + " left the server!"
        ));
    }
}
