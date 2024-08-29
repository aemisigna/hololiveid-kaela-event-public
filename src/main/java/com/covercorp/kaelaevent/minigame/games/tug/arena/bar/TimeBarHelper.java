package com.covercorp.kaelaevent.minigame.games.tug.arena.bar;

import com.covercorp.kaelaevent.minigame.games.tug.TugMiniGame;
import com.covercorp.kaelaevent.minigame.games.tug.arena.TugArena;
import com.covercorp.kaelaevent.minigame.games.tug.player.TugPlayer;
import com.covercorp.kaelaevent.minigame.games.tug.team.TugTeam;
import com.covercorp.kaelaevent.minigame.games.tug.util.TugMatchUtil;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;

import com.covercorp.kaelaevent.util.simple.StringUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public final class TimeBarHelper {
    private final TugArena arena;
    private final PlayerHelper<TugMiniGame> playerHelper;
    private final TeamHelper<TugMiniGame, TugPlayer> teamHelper;

    private final BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);

    private int bossbarTask;

    public TimeBarHelper(final TugArena arena) {
        this.arena = arena;

        playerHelper = arena.getPlayerHelper();
        teamHelper = arena.getTeamHelper();
    }

    public void start() {
        bossBar.setTitle(StringUtils.translate("Tug of War Time Bar"));

        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);

        bossbarTask = Bukkit.getScheduler().runTaskTimer(arena.getTugMiniGame().getKaelaEvent(), () -> {
            final TugTeam team1 = (TugTeam) teamHelper.getTeamList().get(0);
            final TugTeam team2 = (TugTeam) teamHelper.getTeamList().get(1);

            double time = (double) arena.getTimeLimit() / arena.getTimeLimit();

            if (time >= 0.0 && time <= 1.0) {
                bossBar.setTitle(StringUtils.translate(
                        LegacyComponentSerializer.legacyAmpersand().serialize(team1.getBetterPrefix()) + "&e(" + team1.getTeamScore() + " pts.)" +
                        " &7[ " + TugMatchUtil.getVersusBar(team1, team2) + " &7] " +
                        "&e(" + team2.getTeamScore() + " pts.) &f" + LegacyComponentSerializer.legacyAmpersand().serialize(team2.getBetterPrefix())
                ));
                bossBar.setProgress(time);
            } else {
                Bukkit.getScheduler().cancelTask(bossbarTask);
            }

            Bukkit.getOnlinePlayers().forEach(player -> {
                if (!bossBar.getPlayers().contains(player)) bossBar.addPlayer(player);
            });
        }, 10L, 10L).getTaskId();
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(bossbarTask);

        bossBar.removeAll();
    }
}
