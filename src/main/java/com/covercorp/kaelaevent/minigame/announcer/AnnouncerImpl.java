package com.covercorp.kaelaevent.minigame.announcer;

import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import com.covercorp.kaelaevent.util.simple.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class AnnouncerImpl<T extends MiniGame> implements Announcer<T> {
    @Override
    public void sendGlobalComponent(Component component) {
        Bukkit.broadcast(component);
    }

    @Override
    public void sendGlobalMessage(String message, boolean center) {
        if (center) {
            StringUtils.sendGlobalCenteredMessage(message);
        } else {
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(StringUtils.translate(message)));
        }
    }

    @Override
    public void sendGlobalTitle(Title title) {
        Bukkit.getOnlinePlayers().forEach(player -> player.showTitle(title));
    }

    @Override
    public void sendGlobalActionBar(Component component) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(component));
    }

    @Override
    public void sendGlobalSound(Sound sound, float volume, float pitch) {
        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, sound, volume, pitch));
    }

    @Override
    public void sendGlobalSound(String sound, float volume, float pitch) {
        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, sound, volume, pitch));
    }

    @Override
    public void sendTeamComponent(MiniGameTeam<MiniGamePlayer<T>> team, Component component) {
        team.getPlayers().forEach(teamPlayer -> {
            final Player player = Bukkit.getPlayer(teamPlayer.getUniqueId());
            if (player == null) return;

            player.sendMessage(component);
        });
    }

    @Override
    public void sendTeamMessage(MiniGameTeam<MiniGamePlayer<T>> team, String message, boolean center) {
        team.getPlayers().forEach(teamPlayer -> {
            final Player player = Bukkit.getPlayer(teamPlayer.getUniqueId());
            if (player == null) return;

            if (center) {
                StringUtils.sendCenteredMessage(player, message);
            } else {
                player.sendMessage(StringUtils.translate(message));
            }
        });
    }

    @Override
    public void sendTeamTitle(MiniGameTeam<MiniGamePlayer<T>> team, Title title) {
        team.getPlayers().forEach(teamPlayer -> {
            final Player player = Bukkit.getPlayer(teamPlayer.getUniqueId());
            if (player == null) return;

            player.showTitle(title);
        });
    }

    @Override
    public void sendTeamSound(MiniGameTeam<MiniGamePlayer<T>> team, Sound sound, float volume, float pitch) {
        team.getPlayers().forEach(teamPlayer -> {
            final Player player = Bukkit.getPlayer(teamPlayer.getUniqueId());
            if (player == null) return;

            player.playSound(player, sound, volume, pitch);
        });
    }
}
