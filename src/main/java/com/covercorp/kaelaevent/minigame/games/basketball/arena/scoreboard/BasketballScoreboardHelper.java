package com.covercorp.kaelaevent.minigame.games.basketball.arena.scoreboard;

import com.covercorp.kaelaevent.minigame.games.basketball.arena.BasketballArena;

import com.covercorp.kaelaevent.minigame.games.basketball.arena.scoreboard.listener.BasketballScoreboardListener;
import com.covercorp.kaelaevent.minigame.games.basketball.player.BasketballPlayer;
import com.covercorp.kaelaevent.minigame.games.basketball.team.BasketballTeam;
import com.covercorp.kaelaevent.util.TimeUtils;
import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Getter(AccessLevel.PUBLIC)
public final class BasketballScoreboardHelper {
    private final BasketballArena arena;

    private final Map<UUID, FastBoard> boards = new ConcurrentHashMap<>();

    public BasketballScoreboardHelper(final BasketballArena arena) {
        this.arena = arena;

        Bukkit.getServer().getPluginManager().registerEvents(new BasketballScoreboardListener(this), arena.getBasketballMiniGame().getKaelaEvent());

        Bukkit.getServer().getScheduler().runTaskTimer(arena.getBasketballMiniGame().getKaelaEvent(), () -> {
            for (final FastBoard board : boards.values()) updateBoard(board);
        }, 0, 20);
    }

    private void updateBoard(final FastBoard board) {
        final MiniMessage miniMessage = arena.getGameMiniMessage();

        switch (arena.getState()) {
            case WAITING -> board.updateLines(
                    Component.empty(),
                    miniMessage.deserialize("No one is playing right now!"),
                    Component.empty()
            );
            case STARTING, ARENA_STARTING -> board.updateLines(
                    Component.empty(),
                    miniMessage.deserialize("Game starting..."),
                    Component.empty()
            );
            case GAME -> {
                final List<Component> lines = new ArrayList<>();
                lines.add(Component.empty());
                lines.add(miniMessage.deserialize("<white>Time left: <green>" + TimeUtils.formatTime(arena.getTimeLeft())));
                lines.add(miniMessage.deserialize("<white>Basketballs generating in: <green>" + arena.getBasketballMatchProperties().getBallSpawnCooldown() + "s"));
                lines.add(Component.empty());
                lines.add(miniMessage.deserialize("<green><underlined>Score top:"));

                final List<BasketballPlayer> basketballPlayers = arena.getPlayerHelper().getPlayerList()
                        .stream()
                        .map(genericPlayer -> (BasketballPlayer) genericPlayer)
                        .sorted(Comparator.comparingInt(BasketballPlayer::getScore).reversed())
                        .toList();
                final List<Optional<BasketballPlayer>> podium = IntStream.range(0, 3)
                        .mapToObj(i -> i < basketballPlayers.size() ? Optional.of(basketballPlayers.get(i)) : Optional.<BasketballPlayer>empty())
                        .toList();


                final int[] place = { 1 };
                podium.forEach(optPlayer -> {
                    if (optPlayer.isPresent()) {
                        final BasketballPlayer player = optPlayer.get();
                        int score = player.getScore();

                        lines.add(miniMessage.deserialize(" <gray>#" + place[0] + " <aqua>" + player.getName() + " <yellow>[" + score + " pts]"));
                    }
                    place[0]++;
                });

                lines.add(Component.empty());

                board.updateLines(lines);
            }
            case ENDING -> {
                final BasketballTeam winner = arena.getPossibleWinnerTeam();
                if (winner == null) {
                    board.updateLines(
                            Component.empty(),
                            miniMessage.deserialize("<white>No winner detected!"),
                            Component.empty()
                    );
                    return;
                }
                board.updateLines(
                        Component.empty(),
                        miniMessage.deserialize("<white>Winner team: "),
                        winner.getBetterPrefix(),
                        Component.empty()
                );
            }
        }
    }
}
