package com.covercorp.kaelaevent.minigame.games.squid.command;

import com.covercorp.kaelaevent.minigame.games.squid.SquidMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SquidChooseTeamCommand implements CommandExecutor {
    private final SquidMiniGame squidMiniGame;

    public SquidChooseTeamCommand(final SquidMiniGame squidMiniGame) {
        this.squidMiniGame = squidMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        squidMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
