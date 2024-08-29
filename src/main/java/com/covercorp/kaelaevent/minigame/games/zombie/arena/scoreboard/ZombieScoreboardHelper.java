package com.covercorp.kaelaevent.minigame.games.zombie.arena.scoreboard;

import com.covercorp.kaelaevent.minigame.games.zombie.arena.ZombieArena;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.scoreboard.listener.ZombieScoreboardListener;
import com.covercorp.kaelaevent.minigame.games.zombie.player.ZombiePlayer;
import com.covercorp.kaelaevent.minigame.games.zombie.team.ZombieTeam;
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
public final class ZombieScoreboardHelper {
    private final ZombieArena arena;

    private final Map<UUID, FastBoard> boards = new ConcurrentHashMap<>();

    public ZombieScoreboardHelper(final ZombieArena arena) {
        this.arena = arena;

        Bukkit.getServer().getPluginManager().registerEvents(new ZombieScoreboardListener(this), arena.getZombieMiniGame().getKaelaEvent());

        Bukkit.getServer().getScheduler().runTaskTimer(arena.getZombieMiniGame().getKaelaEvent(), () -> {
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
                lines.add(miniMessage.deserialize("<green><underlined>Score top:"));
                
                final List<ZombieTeam> zombieTeams = this.arena
                        .getTeamHelper()
                        .getTeamList()
                        .stream()
                        .map(genericPlayer -> (ZombieTeam)genericPlayer)
                        .sorted(Comparator.comparingInt(ZombieTeam::getScore).reversed())
                        .toList();

                final List<Optional<ZombieTeam>> podium = IntStream.range(0, 4)
                        .mapToObj(i -> i < zombieTeams.size() ? Optional.of(zombieTeams.get(i)) : Optional.<ZombieTeam>empty())
                        .toList();
                int place = 1;

                for (Optional<ZombieTeam> optTeam : podium) {
                    if (optTeam.isPresent()) {
                        final ZombieTeam team = optTeam.get();
                        final ZombiePlayer player = team.getFirstPlayer();
                        if (player != null) {
                            final int score = team.getScore();
                            lines.add(miniMessage.deserialize(" <gray>#" + place + " <aqua>" + player.getName()).append(miniMessage.deserialize(" <yellow>[" + score + " pts.]")));
                        }
                    }
                    place++;
                }

                lines.add(Component.empty());

                board.updateLines(lines);
            }
            case ENDING -> {
                if (arena.getWinnerTeam() == null) {
                    board.updateLines(Component.empty(), miniMessage.deserialize("<white>No winner detected!"), Component.empty());
                    return;
                }

                if (arena.getWinnerTeam().getFirstPlayer() == null) return;

                final List<Component> lines = new ArrayList<>();

                lines.add(Component.empty());
                lines.add(miniMessage.deserialize("Winner: " + arena.getWinnerTeam().getFirstPlayer().getName()));
                lines.add(Component.empty());
                lines.add(miniMessage.deserialize("Final scores: "));

                final List<ZombieTeam> zombieTeams = this.arena
                        .getTeamHelper()
                        .getTeamList()
                        .stream()
                        .map(genericPlayer -> (ZombieTeam)genericPlayer)
                        .sorted(Comparator.comparingInt(ZombieTeam::getScore).reversed())
                        .toList();

                final List<Optional<ZombieTeam>> podium = IntStream.range(0, 4)
                        .mapToObj(i -> i < zombieTeams.size() ? Optional.of(zombieTeams.get(i)) : Optional.<ZombieTeam>empty())
                        .toList();
                int place = 1;

                for (Optional<ZombieTeam> optTeam : podium) {
                    if (optTeam.isPresent()) {
                        final ZombieTeam team = optTeam.get();
                        final ZombiePlayer player = team.getFirstPlayer();
                        if (player != null) {
                            final int score = team.getScore();
                            lines.add(miniMessage.deserialize(" <gray>#" + place + " <aqua>" + player.getName()).append(miniMessage.deserialize(" <yellow>[" + score + " pts.]")));
                        }
                    }
                    place++;
                }

                lines.add(Component.empty());
                board.updateLines(lines);
            }
        }
    }
}