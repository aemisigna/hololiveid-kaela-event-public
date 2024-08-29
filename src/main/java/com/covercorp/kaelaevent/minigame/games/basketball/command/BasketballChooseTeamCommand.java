package com.covercorp.kaelaevent.minigame.games.basketball.command;

import com.covercorp.kaelaevent.minigame.games.basketball.BasketballMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BasketballChooseTeamCommand implements CommandExecutor {
    private BasketballMiniGame basketballMiniGame;

    public BasketballChooseTeamCommand(final BasketballMiniGame basketballMiniGame) {
        this.basketballMiniGame = basketballMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        basketballMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
