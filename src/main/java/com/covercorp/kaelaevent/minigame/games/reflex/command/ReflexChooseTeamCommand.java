package com.covercorp.kaelaevent.minigame.games.reflex.command;

import com.covercorp.kaelaevent.minigame.games.reflex.ReflexMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ReflexChooseTeamCommand implements CommandExecutor {
    private final ReflexMiniGame reflexMiniGame;

    public ReflexChooseTeamCommand(final ReflexMiniGame reflexMiniGame) {
        this.reflexMiniGame = reflexMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        reflexMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
