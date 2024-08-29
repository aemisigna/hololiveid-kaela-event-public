package com.covercorp.kaelaevent.minigame.player;

import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.player.player.MiniGamePlayer;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PlayerHelperImpl<T extends MiniGame> implements PlayerHelper<T> {
    protected final T miniGame;

    protected final Map<UUID, MiniGamePlayer<T>> players;

    public PlayerHelperImpl(final T miniGame) {
        this.miniGame = miniGame;

        players = new ConcurrentHashMap<>();
    }

    @Override
    public abstract MiniGamePlayer<T> addPlayer(final Player player);

    @Override
    public abstract MiniGamePlayer<T> removePlayer(final UUID uuid);

    @Override
    public Optional<MiniGamePlayer<T>> getPlayer(final UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    @Override
    public Optional<MiniGamePlayer<T>> getOrCreatePlayer(final Player player) {
        final Optional<MiniGamePlayer<T>> genericPlayerOptional = getPlayer(player.getUniqueId());
        if (genericPlayerOptional.isPresent()) return genericPlayerOptional;

        addPlayer(player);

        return getPlayer(player.getUniqueId());
    }

    @Override
    public ImmutableList<MiniGamePlayer<T>> getPlayerList() {
        return ImmutableList.copyOf(players.values());
    }

    @Override
    public void clearPlayerList() {
        getPlayerList().forEach(player -> {
            if (removePlayer(player.getUniqueId()) == null) {
                miniGame.getKaelaEvent().getLogger().severe("Could not unregister player: " + player.getUniqueId());
                return;
            }

            miniGame.getKaelaEvent().getLogger().severe("Unregistered player: " + player.getUniqueId());
        });
    }
}
