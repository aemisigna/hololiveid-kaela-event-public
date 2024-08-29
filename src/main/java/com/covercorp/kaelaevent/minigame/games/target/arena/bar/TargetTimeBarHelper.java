package com.covercorp.kaelaevent.minigame.games.target.arena.bar;

import com.covercorp.kaelaevent.minigame.games.target.TargetMiniGame;
import com.covercorp.kaelaevent.minigame.games.target.arena.TargetArena;
import com.covercorp.kaelaevent.minigame.games.target.player.TargetPlayer;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public final class TargetTimeBarHelper {
    private final TargetArena arena;
    private final PlayerHelper<TargetMiniGame> playerHelper;
    private final TeamHelper<TargetMiniGame, TargetPlayer> teamHelper;

    private final BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);

    private int bossbarTask;

    public TargetTimeBarHelper(final TargetArena arena) {
        this.arena = arena;

        playerHelper = arena.getPlayerHelper();
        teamHelper = arena.getTeamHelper();
    }

    public void start() {
        bossBar.setTitle("");

        Bukkit.getOnlinePlayers().forEach(this.bossBar::addPlayer);

        this.bossbarTask = Bukkit.getScheduler().runTaskTimer(this.arena.getTargetMiniGame().getKaelaEvent(), () -> {
            if (arena.getTimeLeft() > 1) {
                bossBar.setTitle(TimeUtils.formatTime(this.arena.getTimeLeft()));
            } else {
                Bukkit.getScheduler().cancelTask(this.bossbarTask);
            }

            Bukkit.getOnlinePlayers().forEach(player -> {
                if (!this.bossBar.getPlayers().contains(player)) {
                    this.bossBar.addPlayer(player);
                }
            });
        }, 10L, 10L).getTaskId();
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(bossbarTask);

        bossBar.removeAll();
    }
}
