package com.covercorp.kaelaevent.minigame.games.trident.command;

import com.covercorp.kaelaevent.minigame.games.trident.TridentMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TridentChooseTeamCommand implements CommandExecutor {
    private final TridentMiniGame tridentMiniGame;

    public TridentChooseTeamCommand(final TridentMiniGame tridentMiniGame) {
        this.tridentMiniGame = tridentMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        tridentMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}