package com.covercorp.kaelaevent.minigame.games.platformrush.command;

import com.covercorp.kaelaevent.minigame.games.platformrush.PlatformRushMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PlatformRushChooseTeamCommand implements CommandExecutor {
    private PlatformRushMiniGame platformRushMiniGame;

    public PlatformRushChooseTeamCommand(final PlatformRushMiniGame platformRushMiniGame) {
        this.platformRushMiniGame = platformRushMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        platformRushMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
