package com.covercorp.kaelaevent.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class PlayerUtils {
    private final static List<UUID> COOL_PLAYERS = List.of(
            UUID.fromString("cf1ba735-eb9c-4b0a-96a7-5dfd0e0bd06a"),
            UUID.fromString("2d6a3566-1708-49b7-b663-404e9e39429b")
    );

    public static void forceSwingAnimation(final Player player) {
        // Swing animation
        try {
            final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            final PacketContainer swingPacket = protocolManager.createPacket(PacketType.Play.Server.ANIMATION);
            swingPacket.getIntegers().write(0, player.getEntityId());
            swingPacket.getIntegers().write(1, 0);

            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> protocolManager.sendServerPacket(onlinePlayer, swingPacket));
        } catch (final Exception exception) {
            Bukkit.getLogger().severe("Could not send packet for button click animation");
        }
    }

    public static boolean isCool(final Player player) {
        if (COOL_PLAYERS.contains(player.getUniqueId())) return true;

        return player.isOp();
    }

    public static String getBetterPrefix(int i) {
        return "<white><font:minecraft:default>\uE311\uF808</font><font:kaela:hud_offset_-7>" + i + "</font><font:minecraft:default>\uF824\uF824</font></white>";
    }
}
