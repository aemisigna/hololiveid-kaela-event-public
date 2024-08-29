package com.covercorp.kaelaevent.minigame.games.snowball.command;

import com.covercorp.kaelaevent.minigame.games.snowball.SnowballMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SnowballChooseTeamCommand implements CommandExecutor {
    private final SnowballMiniGame snowballMiniGame;

    public SnowballChooseTeamCommand(final SnowballMiniGame snowballMiniGame) {
        this.snowballMiniGame = snowballMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        snowballMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
