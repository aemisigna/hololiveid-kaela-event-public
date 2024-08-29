package com.covercorp.kaelaevent.minigame.games.target.arena.scoreboard;

import com.covercorp.kaelaevent.minigame.games.target.arena.TargetArena;
import com.covercorp.kaelaevent.minigame.games.target.arena.scoreboard.listener.TargetScoreboardListener;
import com.covercorp.kaelaevent.minigame.games.target.team.TargetTeam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

@Getter(AccessLevel.PUBLIC)
public final class TargetScoreboardHelper {
    private final TargetArena arena;

    private final Map<UUID, FastBoard> boards = new ConcurrentHashMap<>();

    public TargetScoreboardHelper(final TargetArena arena) {
        this.arena = arena;

        Bukkit.getServer().getPluginManager().registerEvents(new TargetScoreboardListener(this), arena.getTargetMiniGame().getKaelaEvent());

        Bukkit.getServer().getScheduler().runTaskTimer(arena.getTargetMiniGame().getKaelaEvent(), () -> {
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
                lines.add(miniMessage.deserialize("<white>Targets generating in: <green>" + this.arena.getTargetMatchProperties().getTargetSpawnCooldown() + "s"));
                lines.add(Component.empty());
                lines.add(miniMessage.deserialize("<green><underlined>Score top:"));
                
                final List<TargetTeam> targetTeams = this.arena
                        .getTeamHelper()
                        .getTeamList()
                        .stream()
                        .map(genericPlayer -> (TargetTeam)genericPlayer)
                        .sorted(Comparator.comparingInt(TargetTeam::getScore).reversed())
                        .toList();

                final List<Optional<TargetTeam>> podium = IntStream.range(0, 4)
                        .mapToObj(i -> i < targetTeams.size() ? Optional.of(targetTeams.get(i)) : Optional.<TargetTeam>empty())
                        .toList();
                int place = 1;

                for (Optional<TargetTeam> optTeam : podium) {
                    if (optTeam.isPresent()) {
                        TargetTeam team = optTeam.get();
                        int score = team.getScore();
                        lines.add(
                                miniMessage.deserialize(" <gray>#" + place + " <white>")
                                        .append(team.getBetterPrefix().append(miniMessage.deserialize(" <yellow>[" + score + " pts.]")))
                        );
                    }
                    place++;
                }

                lines.add(Component.empty());

                board.updateLines(lines);
            }
            case ENDING -> {
                if (this.arena.getWinnerResults().isEmpty()) {
                    board.updateLines(Component.empty(), miniMessage.deserialize("<white>No winner detected!"), Component.empty());
                    return;
                }

                final List<Component> lines = new ArrayList<>();

                lines.add(Component.empty());
                lines.add(miniMessage.deserialize("Winner Team: <white>").append(arena.getWinnerResults().getFirst().getBetterPrefix()));
                lines.add(miniMessage.deserialize("Scores: "));
                int i = 1;

                for (final TargetTeam team : arena.getWinnerResults()) {
                    lines.add(
                            miniMessage.deserialize(" <gray>#" + i + " <white>")
                                    .append(team.getBetterPrefix())
                                    .append(miniMessage.deserialize("<#61ffa3>[" + team.getScore() + " pts.]"))
                    );
                    i++;
                }

                lines.add(Component.empty());
                board.updateLines(lines);
            }
        }
    }
}