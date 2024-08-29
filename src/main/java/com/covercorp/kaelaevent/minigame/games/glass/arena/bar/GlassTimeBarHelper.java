package com.covercorp.kaelaevent.minigame.games.glass.arena.bar;

import com.covercorp.kaelaevent.minigame.games.glass.arena.GlassArena;
import com.covercorp.kaelaevent.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public final class GlassTimeBarHelper {
    private final GlassArena arena;
    private final BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
    private int bossbarTask;

    public GlassTimeBarHelper(GlassArena arena) {
        this.arena = arena;
    }

    public void start() {
        this.bossBar.setTitle("");
        Bukkit.getOnlinePlayers().forEach(this.bossBar::addPlayer);
        this.bossbarTask = Bukkit.getScheduler().runTaskTimer(this.arena.getGlassMiniGame().getKaelaEvent(), () -> {
            if (this.arena.getTimeLeft() > 1) {
                this.bossBar.setTitle(TimeUtils.formatTime(this.arena.getTimeLeft()));
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
        Bukkit.getScheduler().cancelTask(this.bossbarTask);
        this.bossBar.removeAll();
    }
}