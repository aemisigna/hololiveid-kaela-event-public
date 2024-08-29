package com.covercorp.kaelaevent.minigame.games.lava.command;

import com.covercorp.kaelaevent.minigame.games.lava.LavaMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class LavaChooseTeamCommand implements CommandExecutor {
    private final LavaMiniGame lavaMiniGame;

    public LavaChooseTeamCommand(final LavaMiniGame lavaMiniGame) {
        this.lavaMiniGame = lavaMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        lavaMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
