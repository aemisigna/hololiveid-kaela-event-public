package com.covercorp.kaelaevent.minigame.games.trident.arena.scoreboard;

import com.covercorp.kaelaevent.minigame.games.trident.arena.TridentArena;
import com.covercorp.kaelaevent.minigame.games.trident.arena.scoreboard.listener.TridentScoreboardListener;
import com.covercorp.kaelaevent.minigame.games.trident.team.TridentTeam;
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
public final class TridentScoreboardHelper {
    private final TridentArena arena;

    private final Map<UUID, FastBoard> boards = new ConcurrentHashMap<>();

    public TridentScoreboardHelper(final TridentArena arena) {
        this.arena = arena;

        Bukkit.getServer().getPluginManager().registerEvents(new TridentScoreboardListener(this), arena.getTridentMiniGame().getKaelaEvent());

        Bukkit.getServer().getScheduler().runTaskTimer(arena.getTridentMiniGame().getKaelaEvent(), () -> {
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
                
                final List<TridentTeam> tridentTeams = this.arena
                        .getTeamHelper()
                        .getTeamList()
                        .stream()
                        .map(genericPlayer -> (TridentTeam)genericPlayer)
                        .sorted(Comparator.comparingInt(TridentTeam::getScore).reversed())
                        .toList();

                final List<Optional<TridentTeam>> podium = IntStream.range(0, 4)
                        .mapToObj(i -> i < tridentTeams.size() ? Optional.of(tridentTeams.get(i)) : Optional.<TridentTeam>empty())
                        .toList();
                int place = 1;

                for (Optional<TridentTeam> optTeam : podium) {
                    if (optTeam.isPresent()) {
                        TridentTeam team = optTeam.get();
                        int score = team.getScore();
                        lines.add(miniMessage.deserialize(" <gray>#" + place + " <aqua>" + team.getFirstPlayer().getName() + " <gray>(" + score + "/20)"));
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
                lines.add(miniMessage.deserialize("Winner Team: ").append(arena.getWinnerResults().getFirst().getBetterPrefix()));
                lines.add(miniMessage.deserialize("Scores: "));
                int i = 1;

                for (final TridentTeam team : arena.getWinnerResults()) {
                    lines.add(
                            miniMessage.deserialize(" <gray>#" + i + " <white>")
                                    .append(team.getBetterPrefix())
                                    .append(miniMessage.deserialize("<#61ffa3>(" + team.getScore() + "/20)"))
                    );
                    i++;
                }

                lines.add(Component.empty());
                board.updateLines(lines);
            }
        }
    }
}