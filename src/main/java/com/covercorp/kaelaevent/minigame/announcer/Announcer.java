package com.covercorp.kaelaevent.minigame.announcer;

import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import com.covercorp.kaelaevent.minigame.team.team.MiniGameTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;

public interface Announcer<T extends MiniGame> {
    void sendGlobalComponent(Component component);
    void sendGlobalMessage(String message, boolean center);
    void sendGlobalTitle(Title title);
    void sendGlobalActionBar(Component component);
    void sendGlobalSound(Sound sound, float volume, float pitch);
    void sendGlobalSound(String sound, float volume, float pitch);

    void sendTeamComponent(MiniGameTeam<MiniGamePlayer<T>> team, Component component);
    void sendTeamMessage(MiniGameTeam<MiniGamePlayer<T>> team, String message, boolean center);
    void sendTeamTitle(MiniGameTeam<MiniGamePlayer<T>> team, Title title);
    void sendTeamSound(MiniGameTeam<MiniGamePlayer<T>> team, Sound sound, float volume, float pitch);
}
