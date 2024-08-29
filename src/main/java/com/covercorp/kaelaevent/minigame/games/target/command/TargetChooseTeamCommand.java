package com.covercorp.kaelaevent.minigame.games.target.command;

import com.covercorp.kaelaevent.minigame.games.target.TargetMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TargetChooseTeamCommand implements CommandExecutor {
    private final TargetMiniGame targetMiniGame;

    public TargetChooseTeamCommand(final TargetMiniGame targetMiniGame) {
        this.targetMiniGame = targetMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        targetMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
