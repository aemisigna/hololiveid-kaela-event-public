package com.covercorp.kaelaevent.util;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.util.simple.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.time.Duration;

public final class BungeeUtils {
    public static void sendPlayerToServer(final Player player, final String server) {
        player.showTitle(Title.title(
                Component.text("\uE299\uE299\uE299\uE299\uE299\uE299\uE299"),
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(500),
                        Duration.ofSeconds(10000),
                        Duration.ofMillis(500)
                )
        ));

        Bukkit.getScheduler().runTaskLater(KaelaEvent.getKaelaEvent(), () -> {
            try {
                final ByteArrayOutputStream b = new ByteArrayOutputStream();
                final DataOutputStream out = new DataOutputStream(b);

                out.writeUTF("Connect");
                out.writeUTF(server);

                player.sendPluginMessage(KaelaEvent.getKaelaEvent(),"BungeeCord", b.toByteArray());

                b.close();
                out.close();
            }
            catch (Exception e) {
                player.sendMessage(StringUtils.translate("&cAn error occurred while connecting to the server!"));
            }
        }, 30L);
    }
}