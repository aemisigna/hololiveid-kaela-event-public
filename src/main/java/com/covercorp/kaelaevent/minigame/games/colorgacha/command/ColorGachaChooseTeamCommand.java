package com.covercorp.kaelaevent.minigame.games.colorgacha.command;

import com.covercorp.kaelaevent.minigame.games.colorgacha.ColorGachaMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ColorGachaChooseTeamCommand implements CommandExecutor {
    private ColorGachaMiniGame colorGachaMiniGame;

    public ColorGachaChooseTeamCommand(final ColorGachaMiniGame colorGachaMiniGame) {
        this.colorGachaMiniGame = colorGachaMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        colorGachaMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
