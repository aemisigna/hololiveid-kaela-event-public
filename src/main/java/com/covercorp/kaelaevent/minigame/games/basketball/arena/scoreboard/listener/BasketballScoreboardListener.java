package com.covercorp.kaelaevent.minigame.games.basketball.arena.scoreboard.listener;

import com.covercorp.kaelaevent.minigame.games.basketball.arena.scoreboard.BasketballScoreboardHelper;

import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BasketballScoreboardListener implements Listener {
    private final BasketballScoreboardHelper basketballScoreboardHelper;

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();

        final FastBoard board = new FastBoard(player);

        board.updateTitle(basketballScoreboardHelper.getArena().getGameMiniMessage().deserialize(
                "<yellow><bold>Basketball Shooters"
        ));

        basketballScoreboardHelper.getBoards().put(player.getUniqueId(), board);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        FastBoard board = basketballScoreboardHelper.getBoards().remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }
}
