package com.covercorp.kaelaevent.minigame.games.trident.arena.scoreboard.listener;

import com.covercorp.kaelaevent.minigame.games.trident.arena.scoreboard.TridentScoreboardHelper;
import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TridentScoreboardListener implements Listener {
    private final TridentScoreboardHelper tridentScoreboardHelper;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final FastBoard board = new FastBoard(player);

        board.updateTitle(tridentScoreboardHelper.getArena().getGameMiniMessage().deserialize("<yellow><bold>Trident Race"));

        tridentScoreboardHelper.getBoards().put(player.getUniqueId(), board);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FastBoard board = tridentScoreboardHelper.getBoards().remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }
}
