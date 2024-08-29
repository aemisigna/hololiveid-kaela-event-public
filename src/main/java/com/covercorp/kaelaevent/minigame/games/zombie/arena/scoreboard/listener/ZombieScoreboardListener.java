package com.covercorp.kaelaevent.minigame.games.zombie.arena.scoreboard.listener;

import com.covercorp.kaelaevent.minigame.games.zombie.arena.scoreboard.ZombieScoreboardHelper;
import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ZombieScoreboardListener implements Listener {
    private final ZombieScoreboardHelper zombieScoreboardHelper;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final FastBoard board = new FastBoard(player);

        board.updateTitle(zombieScoreboardHelper.getArena().getGameMiniMessage().deserialize("<yellow><bold>The Zombie Range"));

        zombieScoreboardHelper.getBoards().put(player.getUniqueId(), board);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FastBoard board = zombieScoreboardHelper.getBoards().remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }
}
