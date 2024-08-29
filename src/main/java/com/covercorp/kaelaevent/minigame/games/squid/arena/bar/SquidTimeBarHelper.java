package com.covercorp.kaelaevent.minigame.games.squid.arena.bar;

import com.covercorp.kaelaevent.minigame.games.squid.SquidMiniGame;
import com.covercorp.kaelaevent.minigame.games.squid.arena.SquidArena;
import com.covercorp.kaelaevent.minigame.games.squid.player.SquidPlayer;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public final class SquidTimeBarHelper {
    private final SquidArena arena;
    private final PlayerHelper<SquidMiniGame> playerHelper;
    private final TeamHelper<SquidMiniGame, SquidPlayer> teamHelper;

    private final BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);

    private int bossbarTask;

    public SquidTimeBarHelper(final SquidArena arena) {
        this.arena = arena;

        playerHelper = arena.getPlayerHelper();
        teamHelper = arena.getTeamHelper();
    }

    public void start() {
        bossBar.setTitle("");

        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);

        this.bossbarTask = Bukkit.getScheduler().runTaskTimer(this.arena.getSquidMiniGame().getKaelaEvent(), () -> {
            if (arena.getTimeLeft() > 1) {
                bossBar.setTitle(TimeUtils.formatTime(arena.getTimeLeft()));
            } else {
                Bukkit.getScheduler().cancelTask(bossbarTask);
            }

            Bukkit.getOnlinePlayers().forEach(player -> {
                if (!this.bossBar.getPlayers().contains(player)) {
                    this.bossBar.addPlayer(player);
                }
            });
        }, 0L, 10L).getTaskId();
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(bossbarTask);

        bossBar.removeAll();
    }
}
