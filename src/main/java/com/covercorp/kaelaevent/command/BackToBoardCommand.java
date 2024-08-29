package com.covercorp.kaelaevent.command;

import com.covercorp.kaelaevent.util.BungeeUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BackToBoardCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        BungeeUtils.sendPlayerToServer((Player) commandSender, "main");
        return false;
    }
}
