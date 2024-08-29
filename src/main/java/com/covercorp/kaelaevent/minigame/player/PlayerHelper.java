package com.covercorp.kaelaevent.minigame.player;

import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface PlayerHelper<T extends MiniGame> {
    MiniGamePlayer<T> addPlayer(Player player);
    MiniGamePlayer<T> removePlayer(UUID uuid);

    Optional<MiniGamePlayer<T>> getPlayer(final UUID uuid);
    Optional<MiniGamePlayer<T>> getOrCreatePlayer(final Player player);

    ImmutableList<MiniGamePlayer<T>> getPlayerList();

    void clearPlayerList();
}
