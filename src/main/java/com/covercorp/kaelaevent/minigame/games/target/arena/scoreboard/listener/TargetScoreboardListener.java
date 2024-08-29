package com.covercorp.kaelaevent.minigame.games.target.arena.scoreboard.listener;

import com.covercorp.kaelaevent.minigame.games.target.arena.scoreboard.TargetScoreboardHelper;
import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TargetScoreboardListener implements Listener {
    private final TargetScoreboardHelper targetScoreboardHelper;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final FastBoard board = new FastBoard(player);

        board.updateTitle(targetScoreboardHelper.getArena().getGameMiniMessage().deserialize("<yellow><bold>Target Shooting Games"));

        targetScoreboardHelper.getBoards().put(player.getUniqueId(), board);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FastBoard board = targetScoreboardHelper.getBoards().remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }
}
