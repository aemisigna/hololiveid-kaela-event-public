package com.covercorp.kaelaevent.minigame.games.tower.command;

import com.covercorp.kaelaevent.minigame.games.tower.TowerMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TowerChooseTeamCommand implements CommandExecutor {
    private final TowerMiniGame towerMiniGame;

    public TowerChooseTeamCommand(final TowerMiniGame towerMiniGame) {
        this.towerMiniGame = towerMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        towerMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
