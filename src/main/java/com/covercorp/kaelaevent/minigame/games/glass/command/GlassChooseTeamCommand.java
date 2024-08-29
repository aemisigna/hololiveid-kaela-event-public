package com.covercorp.kaelaevent.minigame.games.glass.command;

import com.covercorp.kaelaevent.minigame.games.glass.GlassMiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class GlassChooseTeamCommand implements CommandExecutor {
    private final GlassMiniGame glassMiniGame;

    public GlassChooseTeamCommand(final GlassMiniGame glassMiniGame) {
        this.glassMiniGame = glassMiniGame;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        glassMiniGame.openTeamInventory((Player) commandSender);
        return false;
    }
}
