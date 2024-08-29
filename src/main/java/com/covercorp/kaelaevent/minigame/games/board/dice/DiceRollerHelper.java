package com.covercorp.kaelaevent.minigame.games.board.dice;

import com.covercorp.kaelaevent.minigame.games.board.dice.type.DiceRollType;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface DiceRollerHelper {
    void roll(Player player, DiceRollType diceRollType);
    void cancelRoll(Player player);
    ImmutableList<UUID> getRollers();
}
