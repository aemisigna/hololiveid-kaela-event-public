package com.covercorp.kaelaevent.minigame.games.board;

import com.covercorp.kaelaevent.KaelaEvent;
import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.games.board.dice.DiceRollerHelper;
import com.covercorp.kaelaevent.minigame.games.board.dice.DiceRollerHelperImpl;
import com.covercorp.kaelaevent.minigame.games.board.inventory.SlotTeleportGUI;
import com.covercorp.kaelaevent.minigame.games.board.listener.BoardEnvironmentListener;
import com.covercorp.kaelaevent.minigame.games.board.listener.BoardItemListener;
import com.covercorp.kaelaevent.minigame.type.MiniGameType;
import org.bukkit.Bukkit;

public final class MainBoardMiniGame extends MiniGame {
    private final DiceRollerHelper diceRollerHelper;

    public MainBoardMiniGame(final KaelaEvent kaelaEvent) {
        super(kaelaEvent, MiniGameType.BOARD);

        diceRollerHelper = new DiceRollerHelperImpl(this);
    }

    @Override
    public void onGameLoad() {
        Bukkit.getServer().getPluginManager().registerEvents(new BoardEnvironmentListener(), getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new BoardItemListener(), getKaelaEvent());

        getKaelaEvent().getLogger().info("Game loaded: Snakes & Ladders board.");
    }

    @Override
    public void onGameUnload() {
        getKaelaEvent().getLogger().info("Game unloaded: Snakes & Ladders board.");
    }
}
