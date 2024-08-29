package com.covercorp.kaelaevent.minigame.games.zombie.command;

import com.covercorp.kaelaevent.minigame.games.zombie.ZombieMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ZombieChooseTeamCommand implements CommandExecutor {
    private final ZombieMiniGame zombieMiniGame;

    public ZombieChooseTeamCommand(final ZombieMiniGame zombieMiniGame) {
        this.zombieMiniGame = zombieMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        zombieMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
