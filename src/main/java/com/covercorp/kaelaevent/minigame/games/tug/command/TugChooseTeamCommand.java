package com.covercorp.kaelaevent.minigame.games.tug.command;

import com.covercorp.kaelaevent.minigame.games.tug.TugMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TugChooseTeamCommand implements CommandExecutor {
    private final TugMiniGame tugMiniGame;

    public TugChooseTeamCommand(final TugMiniGame tugMiniGame) {
        this.tugMiniGame = tugMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        tugMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
