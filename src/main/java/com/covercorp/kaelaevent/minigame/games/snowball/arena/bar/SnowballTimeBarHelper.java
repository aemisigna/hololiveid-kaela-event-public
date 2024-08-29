package com.covercorp.kaelaevent.minigame.games.snowball.arena.bar;

import com.covercorp.kaelaevent.minigame.games.snowball.SnowballMiniGame;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.SnowballArena;
import com.covercorp.kaelaevent.minigame.games.snowball.player.SnowballPlayer;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public final class SnowballTimeBarHelper {
    private final SnowballArena arena;
    private final PlayerHelper<SnowballMiniGame> playerHelper;
    private final TeamHelper<SnowballMiniGame, SnowballPlayer> teamHelper;

    private final BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);

    private int bossbarTask;

    public SnowballTimeBarHelper(final SnowballArena arena) {
        this.arena = arena;

        playerHelper = arena.getPlayerHelper();
        teamHelper = arena.getTeamHelper();
    }

    public void start() {
        bossBar.setTitle("");

        Bukkit.getOnlinePlayers().forEach(this.bossBar::addPlayer);

        this.bossbarTask = Bukkit.getScheduler().runTaskTimer(this.arena.getSnowballMiniGame().getKaelaEvent(), () -> {
            bossBar.setTitle(TimeUtils.formatTime(arena.getSnowballMatchProperties().getTntCooldown()));

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
