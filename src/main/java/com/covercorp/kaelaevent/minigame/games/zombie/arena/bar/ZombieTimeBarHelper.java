package com.covercorp.kaelaevent.minigame.games.zombie.arena.bar;

import com.covercorp.kaelaevent.minigame.games.zombie.ZombieMiniGame;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.ZombieArena;
import com.covercorp.kaelaevent.minigame.games.zombie.player.ZombiePlayer;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public final class ZombieTimeBarHelper {
    private final ZombieArena arena;
    private final PlayerHelper<ZombieMiniGame> playerHelper;
    private final TeamHelper<ZombieMiniGame, ZombiePlayer> teamHelper;

    private final BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);

    private int bossbarTask;

    public ZombieTimeBarHelper(final ZombieArena arena) {
        this.arena = arena;

        playerHelper = arena.getPlayerHelper();
        teamHelper = arena.getTeamHelper();
    }

    public void start() {
        bossBar.setTitle("");

        Bukkit.getOnlinePlayers().forEach(this.bossBar::addPlayer);

        this.bossbarTask = Bukkit.getScheduler().runTaskTimer(this.arena.getZombieMiniGame().getKaelaEvent(), () -> {
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
