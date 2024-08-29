package com.covercorp.kaelaevent.minigame.games.tower.arena.bar;

import com.covercorp.kaelaevent.minigame.games.tower.TowerMiniGame;
import com.covercorp.kaelaevent.minigame.games.tower.arena.TowerArena;
import com.covercorp.kaelaevent.minigame.games.tower.player.TowerPlayer;
import com.covercorp.kaelaevent.minigame.games.tower.team.TowerTeam;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.simple.StringUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public final class TowerScoreBarHelper {
    private final TowerArena arena;
    private final PlayerHelper<TowerMiniGame> playerHelper;
    private final TeamHelper<TowerMiniGame, TowerPlayer> teamHelper;

    private final BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);

    private int bossbarTask;

    private static final int TOTAL_POINTS = 5;
    private static final String TAKEN_POINT = "&a⬤&r";
    private static final String EMPTY_POINT = "&7⬤&r";

    public TowerScoreBarHelper(final TowerArena arena) {
        this.arena = arena;

        playerHelper = arena.getPlayerHelper();
        teamHelper = arena.getTeamHelper();
    }

    public void start() {
        bossBar.setTitle("");

        Bukkit.getOnlinePlayers().forEach(this.bossBar::addPlayer);

        bossbarTask = Bukkit.getScheduler().runTaskTimer(arena.getTowerMiniGame().getKaelaEvent(), () -> {
            if (arena.getTeamHelper().getTeamList().size() < 2) return;

            final TowerTeam team1 = (TowerTeam) arena.getTeamHelper().getTeamList().get(0);
            final TowerTeam team2 = (TowerTeam) arena.getTeamHelper().getTeamList().get(1);

            bossBar.setTitle(StringUtils.translate(
                    "&b" + team1.getFirstPlayer().getName() + " &f" +
                    LegacyComponentSerializer.legacyAmpersand().serialize(team1.getBetterPrefix()) + getBar(team1, true) + " &eVS. &r" +
                    getBar(team2, false) + " &r" + LegacyComponentSerializer.legacyAmpersand().serialize(team2.getBetterPrefix()) +
                    "&b" + team2.getFirstPlayer().getName()
            ));

            Bukkit.getOnlinePlayers().forEach(player -> {
                if (!bossBar.getPlayers().contains(player)) {
                    bossBar.addPlayer(player);
                }
            });
        }, 10L, 10L).getTaskId();
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(bossbarTask);

        bossBar.removeAll();
    }

    public static String getBar(final TowerTeam team, final boolean barSide) {
        final int score = team.getScore();
        final StringBuilder bar = new StringBuilder();

        if (barSide) {
            for (int i = 0; i < TOTAL_POINTS; i++) {
                if (i < score) {
                    bar.append(TAKEN_POINT);
                } else {
                    bar.append(EMPTY_POINT);
                }
            }
        } else {
            for (int i = 0; i < TOTAL_POINTS; i++) {
                if (i < score) {
                    bar.insert(0, TAKEN_POINT);
                } else {
                    bar.insert(0, EMPTY_POINT);
                }
            }
        }

        return bar.toString();
    }
}
